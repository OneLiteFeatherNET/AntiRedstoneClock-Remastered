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
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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

    static Stream<Exception> clientFailures() {
        return Stream.of(new IOException("connection reset"), new InterruptedException("interrupted"));
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

    @ParameterizedTest
    @MethodSource("clientFailures")
    @DisplayName("A failing request is handled as no update without throwing")
    void failingRequestYieldsNoUpdate(Exception failure) throws IOException, InterruptedException {
        doThrow(failure).when(this.hangarClient).send(any(HttpRequest.class), any());

        assertThatCode(this.updateService::run)
                .as("%s from the client must not escape the update check", failure.getClass().getSimpleName())
                .doesNotThrowAnyException();

        verify(this.operator, never().description("no update must be reported after a failing request"))
                .sendMessage(any(Component.class));
        verify(this.consoleLogger, never().description("the console must not be told about an update"))
                .warn(any(Component.class));
    }

    @ParameterizedTest
    @MethodSource("clientFailures")
    @DisplayName("A failing request does not count towards the retry limit")
    void failingRequestDoesNotCountTowardsRetryLimit(Exception failure) throws IOException, InterruptedException {
        ScheduledTask scheduledTask = scheduleWithTask();
        doThrow(failure).when(this.hangarClient).send(any(HttpRequest.class), any());

        for (int attempt = 0; attempt <= MAX_RETRY_COUNT; attempt++) {
            this.updateService.run();
        }

        verify(this.hangarClient, times(MAX_RETRY_COUNT + 1).description("every run must still ask Hangar"))
                .send(any(HttpRequest.class), any());
        verify(scheduledTask, never().description("failing requests must not cancel the scheduled check"))
                .cancel();
    }

    @Test
    @DisplayName("A failing request does not reset the count of failed responses")
    void failingRequestDoesNotResetRetryCount() throws IOException, InterruptedException {
        ScheduledTask scheduledTask = scheduleWithTask();
        HttpResponse<String> unavailable = response(503, "");
        doReturn(unavailable, unavailable, unavailable, unavailable)
                .doThrow(new IOException("connection reset"))
                .doReturn(unavailable)
                .when(this.hangarClient).send(any(HttpRequest.class), any());

        for (int attempt = 0; attempt <= MAX_RETRY_COUNT + 1; attempt++) {
            this.updateService.run();
        }

        verify(this.hangarClient, times(MAX_RETRY_COUNT + 1)
                .description("the check must stop after five failed responses around one failing request"))
                .send(any(HttpRequest.class), any());
        verify(scheduledTask, times(1).description("the scheduled check must be cancelled at the retry limit"))
                .cancel();
    }

    @Test
    @DisplayName("A non-200 response counts towards the retry limit and stops the check once it is reached")
    void failedResponsesStopTheCheckAfterTheRetryLimit() throws IOException, InterruptedException {
        ScheduledTask scheduledTask = scheduleWithTask();
        respondWith(503, "");

        for (int attempt = 0; attempt <= MAX_RETRY_COUNT; attempt++) {
            this.updateService.run();
        }

        verify(this.hangarClient, times(MAX_RETRY_COUNT).description("Hangar must be asked once per allowed retry"))
                .send(any(HttpRequest.class), any());
        verify(scheduledTask, times(1).description("the scheduled check must be cancelled at the retry limit"))
                .cancel();
    }

    @Test
    @DisplayName("A successful response resets the count of failed responses")
    void successfulResponseResetsRetryCount() throws IOException, InterruptedException {
        ScheduledTask scheduledTask = scheduleWithTask();
        HttpResponse<String> unavailable = response(503, "");
        HttpResponse<String> current = response(200, LOCAL_VERSION.toString());
        doReturn(unavailable, unavailable, unavailable, unavailable, current,
                unavailable, unavailable, unavailable, unavailable, unavailable)
                .when(this.hangarClient).send(any(HttpRequest.class), any());

        for (int attempt = 0; attempt < 10; attempt++) {
            this.updateService.run();
        }

        verify(this.hangarClient, times(10).description("the success must allow another five failed responses"))
                .send(any(HttpRequest.class), any());
        verify(scheduledTask, never().description("the check must not be cancelled before five failures in a row"))
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
    @DisplayName("A player holding the update permission is told about a newer version")
    void playerWithPermissionIsNotified() throws IOException, InterruptedException {
        Player admin = mock(Player.class);
        when(admin.hasPermission(Constants.PERMISSION_NOTIFY_UPDATE)).thenReturn(true);
        UpdateService service = new UpdateService(LOCAL_VERSION, this.schedulerService, this.hangarClient,
                this.consoleLogger, () -> List.of(admin));
        respondWith(200, "2.1.0");

        service.run();

        verify(admin, times(1).description("a player with the update permission must be told"))
                .sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("A player without op or the update permission is not told about a newer version")
    void playerWithoutPermissionIsNotNotified() throws IOException, InterruptedException {
        Player player = mock(Player.class);
        UpdateService service = new UpdateService(LOCAL_VERSION, this.schedulerService, this.hangarClient,
                this.consoleLogger, () -> List.of(player));
        respondWith(200, "2.1.0");

        service.run();

        verify(player, never().description("a regular player must not be told about updates"))
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
        this.updateService.run();

        verify(this.consoleLogger, times(1).description("the console must be told once about version 2.1.0"))
                .warn(any(Component.class));
    }

    @Test
    @DisplayName("A still newer version is reported to the console again")
    void stillNewerVersionIsReportedToTheConsoleAgain() throws IOException, InterruptedException {
        doReturn(response(200, "2.1.0"), response(200, "2.1.0"), response(200, "2.2.0"))
                .when(this.hangarClient).send(any(HttpRequest.class), any());

        this.updateService.run();
        this.updateService.run();
        this.updateService.run();

        verify(this.consoleLogger, times(2).description("the console must be told once about 2.1.0 and once about 2.2.0"))
                .warn(any(Component.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.9.9", "2.0.0", "2.0.0-rc.1", "2.0.0+build.7"})
    @DisplayName("An older or equal remote version is not reported")
    void olderOrEqualVersionIsNotReported(String body) throws IOException, InterruptedException {
        respondWith(200, body);

        this.updateService.run();

        verify(this.operator, never().description("remote version " + body + " must not be reported"))
                .sendMessage(any(Component.class));
        verify(this.consoleLogger, never().description("the console must not be told about " + body))
                .warn(any(Component.class));
    }

    @Test
    @DisplayName("A newer pre-release with build metadata is reported without throwing")
    void preReleaseWithBuildMetadataIsReported() throws IOException, InterruptedException {
        respondWith(200, "2.1.0-beta.1+build.5");

        assertThatCode(this.updateService::run)
                .as("a pre-release with build metadata must be parsed")
                .doesNotThrowAnyException();

        verify(this.operator, times(1).description("2.1.0-beta.1 is newer than 2.0.0 and must be reported"))
                .sendMessage(any(Component.class));
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

    @Test
    @DisplayName("The Hangar request reads the latest release with GET")
    void hangarRequestTargetsLatestRelease() {
        HttpRequest request = Constants.LATEST_RELEASE_VERSION_REQUEST;

        assertThat(request.method())
                .as("the latest release must be read with GET")
                .isEqualTo("GET");
        assertThat(request.uri().toString())
                .as("the request must target the Hangar latest release endpoint")
                .isEqualTo("https://hangar.papermc.io/api/v1/projects/AntiRedstoneClock-Remastered/latestrelease");
    }

    private ScheduledTask scheduleWithTask() {
        ScheduledTask scheduledTask = mock(ScheduledTask.class);
        when(this.schedulerService.runTaskTimerAsynchronously(any(), anyLong(), anyLong())).thenReturn(scheduledTask);
        this.updateService.schedule();
        return scheduledTask;
    }

    private void respondWith(int statusCode, String body) throws IOException, InterruptedException {
        doReturn(response(statusCode, body)).when(this.hangarClient).send(any(HttpRequest.class), any());
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> response(int statusCode, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }
}
