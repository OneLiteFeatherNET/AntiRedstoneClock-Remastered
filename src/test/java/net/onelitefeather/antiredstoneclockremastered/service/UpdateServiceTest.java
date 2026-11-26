package net.onelitefeather.antiredstoneclockremastered.service;

import com.github.zafarkhaja.semver.Version;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.onelitefeather.antiredstoneclockremastered.service.api.SchedulerService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the update check against Hangar without any network access.
 */
class UpdateServiceTest {

    private static final Version LOCAL_VERSION = Version.of(2, 0, 0);
    private static final int MAX_RETRY_COUNT = 5;
    private static final Duration TEN_SECONDS = Duration.ofSeconds(10);

    private HttpClient hangarClient;
    private ComponentLogger consoleLogger;
    private SchedulerService schedulerService;
    private Player operator;
    private UpdateService updateService;

    @BeforeEach
    void setUp() {
        this.hangarClient = mock(HttpClient.class);
        this.consoleLogger = mock(ComponentLogger.class);
        this.schedulerService = mock(SchedulerService.class);
        this.operator = mock(Player.class);
        when(this.operator.isOp()).thenReturn(true);
        this.updateService = new UpdateService(LOCAL_VERSION, this.schedulerService, this.hangarClient,
                this.consoleLogger, () -> List.of(this.operator));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-version", "<html>maintenance</html>"})
    @DisplayName("An unparsable 200 response is handled as no update without throwing")
    void unparsableResponseYieldsNoUpdate(String body) throws IOException, InterruptedException {
        respondWith(200, body);

        assertThatCode(this.updateService::run)
                .as("an unparsable body %s must not escape the update check", body)
                .doesNotThrowAnyException();

        Player joiningPlayer = mock(Player.class);
        this.updateService.notifyPlayer(joiningPlayer);
        verify(joiningPlayer, never().description("no update must be reported for an unparsable body"))
                .sendMessage(any(Component.class));
        verify(this.consoleLogger, never().description("the console must not be told about an update"))
                .warn(any(Component.class));
    }

    @Test
    @DisplayName("A non-200 response counts towards the retry limit and stops the check once it is reached")
    void failedResponsesStopTheCheckAfterTheRetryLimit() throws IOException, InterruptedException {
        ScheduledTask scheduledTask = mock(ScheduledTask.class);
        when(this.schedulerService.runTaskTimerAsynchronously(any(), anyLong(), anyLong())).thenReturn(scheduledTask);
        respondWith(503, "");
        this.updateService.schedule();

        for (int attempt = 0; attempt <= MAX_RETRY_COUNT; attempt++) {
            this.updateService.run();
        }

        verify(this.hangarClient, times(MAX_RETRY_COUNT).description("Hangar must be asked once per allowed retry"))
                .send(any(HttpRequest.class), any());
        verify(scheduledTask, times(1).description("the scheduled check must be cancelled at the retry limit"))
                .cancel();
    }

    @Test
    @DisplayName("A newer version is reported to online operators")
    void newerVersionIsReportedToOperators() throws IOException, InterruptedException {
        respondWith(200, "2.1.0");

        this.updateService.run();

        verify(this.operator, times(1).description("an online operator must be told about the newer version"))
                .sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("A newer version is reported to players joining later")
    void newerVersionIsReportedToJoiningPlayers() throws IOException, InterruptedException {
        respondWith(200, "2.1.0");
        this.updateService.run();

        Player joiningPlayer = mock(Player.class);
        this.updateService.notifyPlayer(joiningPlayer);

        verify(joiningPlayer, times(1).description("a joining player must be told about the newer version"))
                .sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("A newer version found by the asynchronous check is reported to the console once")
    void newerVersionIsReportedToTheConsoleOnce() throws IOException, InterruptedException {
        respondWith(200, "2.1.0");

        this.updateService.run();
        this.updateService.run();

        verify(this.consoleLogger, times(1).description("the console must be told once about version 2.1.0"))
                .warn(any(Component.class));
    }

    @Test
    @DisplayName("The same or an older remote version is not reported")
    void sameVersionIsNotReported() throws IOException, InterruptedException {
        respondWith(200, LOCAL_VERSION.toString());

        this.updateService.run();

        verify(this.operator, never().description("no update must be reported for the installed version"))
                .sendMessage(any(Component.class));
        verify(this.consoleLogger, never().description("the console must not be told about the installed version"))
                .warn(any(Component.class));
    }

    @Test
    @DisplayName("The check is scheduled immediately and repeats every three hours in ticks")
    void checkIsScheduledEveryThreeHours() {
        this.updateService.schedule();

        verify(this.schedulerService).runTaskTimerAsynchronously(any(), eq(0L), eq(20L * 60 * 60 * 3));
    }

    @Test
    @DisplayName("The Hangar client gives up connecting after ten seconds")
    void hangarClientHasConnectTimeout() {
        assertThat(UpdateService.createHangarClient().connectTimeout())
                .as("connecting to Hangar must not block forever")
                .contains(TEN_SECONDS);
    }

    @Test
    @DisplayName("The Hangar request gives up after ten seconds")
    void hangarRequestHasTimeout() {
        assertThat(Constants.LATEST_RELEASE_VERSION_REQUEST.timeout())
                .as("waiting for the Hangar response must not block forever")
                .contains(TEN_SECONDS);
    }

    @SuppressWarnings("unchecked")
    private void respondWith(int statusCode, String body) throws IOException, InterruptedException {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        doReturn(response).when(this.hangarClient).send(any(HttpRequest.class), any());
    }
}
