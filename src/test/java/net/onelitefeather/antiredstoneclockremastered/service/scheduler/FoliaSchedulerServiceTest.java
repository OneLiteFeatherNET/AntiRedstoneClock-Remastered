package net.onelitefeather.antiredstoneclockremastered.service.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.bukkit.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that the Folia scheduler service hands tick based timings to the asynchronous scheduler correctly.
 */
class FoliaSchedulerServiceTest {

    private AntiRedstoneClockRemastered plugin;
    private AsyncScheduler asyncScheduler;
    private FoliaSchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        this.plugin = mock(AntiRedstoneClockRemastered.class);
        this.asyncScheduler = mock(AsyncScheduler.class);
        Server server = mock(Server.class);
        when(this.plugin.getServer()).thenReturn(server);
        when(server.getAsyncScheduler()).thenReturn(this.asyncScheduler);
        this.schedulerService = new FoliaSchedulerService(this.plugin);
    }

    @Test
    @DisplayName("Asynchronous timer period in ticks is converted to milliseconds")
    void asyncTimerConvertsPeriodToMilliseconds() {
        this.schedulerService.runTaskTimerAsynchronously(task -> { }, 0L, 216_000L);

        verify(this.asyncScheduler).runAtFixedRate(same(this.plugin), any(), eq(0L), eq(10_800_000L),
                eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("Asynchronous timer delay in ticks is converted to milliseconds")
    void asyncTimerConvertsDelayToMilliseconds() {
        this.schedulerService.runTaskTimerAsynchronously(task -> { }, 20L, 1L);

        verify(this.asyncScheduler).runAtFixedRate(same(this.plugin), any(), eq(1_000L), eq(50L),
                eq(TimeUnit.MILLISECONDS));
    }
}
