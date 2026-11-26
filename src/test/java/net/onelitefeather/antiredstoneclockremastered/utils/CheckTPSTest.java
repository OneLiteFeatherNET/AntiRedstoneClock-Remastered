package net.onelitefeather.antiredstoneclockremastered.utils;

import com.google.inject.Guice;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.SchedulerService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests how the TPS check turns the time between two samples into a tick rate and whether detection keeps running.
 * Time is driven by a fake nanosecond source, the scheduled sample task is invoked directly.
 */
class CheckTPSTest {

    /** Arbitrary, non-zero start so nothing relies on the time source starting at zero. */
    private static final long START_NANOS = 987_654_321_000L;
    /** The first sample runs one tick after the check is started. */
    private static final long ONE_TICK_MILLIS = 50;

    private long nowNanos = START_NANOS;
    private CheckTPS checkTPS;
    private Runnable sample;

    private void start(int interval, int min, int max) {
        var config = new YamlConfiguration();
        config.set("tps.interval", interval);
        config.set("tps.min", min);
        config.set("tps.max", max);
        var plugin = mock(AntiRedstoneClockRemastered.class);
        when(plugin.getConfig()).thenReturn(config);
        var scheduler = mock(SchedulerService.class);
        this.checkTPS = new CheckTPS(plugin, scheduler, () -> this.nowNanos);
        this.checkTPS.startCheck();
        var task = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).scheduleRepeatingTask(task.capture(), anyLong(), anyLong());
        this.sample = task.getValue();
    }

    private void startWithDefaults() {
        start(2, 15, 20);
    }

    private void sampleAfterMillis(long millis) {
        this.nowNanos += TimeUnit.MILLISECONDS.toNanos(millis);
        this.sample.run();
    }

    /** Takes the first sample one tick after start, then a second one {@code millis} later. */
    private void measureWindowOfMillis(long millis) {
        sampleAfterMillis(ONE_TICK_MILLIS);
        sampleAfterMillis(millis);
    }

    @Test
    @DisplayName("A window one millisecond shorter than the interval counts as 20 TPS and keeps detection running")
    void slightlyShortWindowKeepsDetectionRunning() {
        startWithDefaults();

        measureWindowOfMillis(1999);

        assertThat(this.checkTPS.getTps()).as("1999 ms for 40 ticks is 20 TPS").isEqualTo(20);
        assertThat(this.checkTPS.isTpsOk()).as("a healthy server must not pause detection").isTrue();
    }

    @Test
    @DisplayName("A window one millisecond longer than the interval counts as 20 TPS and keeps detection running")
    void slightlyLongWindowKeepsDetectionRunning() {
        startWithDefaults();

        measureWindowOfMillis(2001);

        assertThat(this.checkTPS.getTps()).as("2001 ms for 40 ticks is 20 TPS").isEqualTo(20);
        assertThat(this.checkTPS.isTpsOk()).as("a healthy server must not pause detection").isTrue();
    }

    @Test
    @DisplayName("A lagging server below the default minimum pauses detection")
    void laggingServerPausesDetection() {
        startWithDefaults();

        measureWindowOfMillis(3000);

        assertThat(this.checkTPS.getTps()).as("3000 ms for 40 ticks is about 13 TPS").isEqualTo(13);
        assertThat(this.checkTPS.isTpsOk()).as("13 TPS is below tps.min 15").isFalse();
    }

    @Test
    @DisplayName("The first sample only sets the baseline and reports a nominal, running state")
    void firstSampleIsSane() {
        startWithDefaults();

        sampleAfterMillis(ONE_TICK_MILLIS);

        assertThat(this.checkTPS.getTps()).as("no window has been measured yet").isEqualTo(20);
        assertThat(this.checkTPS.isTpsOk()).as("the first sample must not pause detection").isTrue();
    }

    @Test
    @DisplayName("A negative minimum disables the lower bound")
    void negativeMinimumDisablesLowerBound() {
        start(2, -1, 20);

        measureWindowOfMillis(4000);

        assertThat(this.checkTPS.getTps()).as("4000 ms for 40 ticks is 10 TPS").isEqualTo(10);
        assertThat(this.checkTPS.isTpsOk()).as("without a lower bound 10 TPS is accepted").isTrue();
    }

    @Test
    @DisplayName("A negative minimum still enforces the maximum")
    void negativeMinimumStillEnforcesMaximum() {
        start(2, -1, 20);

        measureWindowOfMillis(1000);

        assertThat(this.checkTPS.getTps()).as("1000 ms for 40 ticks is 40 TPS").isEqualTo(40);
        assertThat(this.checkTPS.isTpsOk()).as("40 TPS is above tps.max 20").isFalse();
    }

    @Test
    @DisplayName("A negative maximum disables the upper bound")
    void negativeMaximumDisablesUpperBound() {
        start(2, 15, -1);

        measureWindowOfMillis(1000);

        assertThat(this.checkTPS.getTps()).as("1000 ms for 40 ticks is 40 TPS").isEqualTo(40);
        assertThat(this.checkTPS.isTpsOk()).as("without an upper bound 40 TPS is accepted").isTrue();
    }

    @Test
    @DisplayName("A negative maximum still enforces the minimum")
    void negativeMaximumStillEnforcesMinimum() {
        start(2, 15, -1);

        measureWindowOfMillis(4000);

        assertThat(this.checkTPS.getTps()).as("4000 ms for 40 ticks is 10 TPS").isEqualTo(10);
        assertThat(this.checkTPS.isTpsOk()).as("10 TPS is below tps.min 15").isFalse();
    }

    @Test
    @DisplayName("Guice creates the check through its injectable constructor")
    void guiceCreatesTheCheck() {
        var plugin = mock(AntiRedstoneClockRemastered.class);
        when(plugin.getConfig()).thenReturn(new YamlConfiguration());
        var scheduler = mock(SchedulerService.class);
        var injector = Guice.createInjector(binder -> {
            binder.bind(AntiRedstoneClockRemastered.class).toInstance(plugin);
            binder.bind(SchedulerService.class).toInstance(scheduler);
        });

        var created = injector.getInstance(CheckTPS.class);

        assertThat(created.isTpsOk()).as("a freshly created check lets detection run").isTrue();
    }
}
