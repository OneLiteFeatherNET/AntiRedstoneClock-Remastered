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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private SchedulerService scheduler;
    private Runnable sample;

    private void create(int interval, int min, int max) {
        var config = new YamlConfiguration();
        config.set("tps.interval", interval);
        config.set("tps.min", min);
        config.set("tps.max", max);
        var plugin = mock(AntiRedstoneClockRemastered.class);
        when(plugin.getConfig()).thenReturn(config);
        this.scheduler = mock(SchedulerService.class);
        this.checkTPS = new CheckTPS(plugin, this.scheduler, () -> this.nowNanos);
    }

    private void start(int interval, int min, int max) {
        create(interval, min, max);
        this.checkTPS.startCheck();
        var task = ArgumentCaptor.forClass(Runnable.class);
        verify(this.scheduler).scheduleRepeatingTask(task.capture(), anyLong(), anyLong());
        this.sample = task.getValue();
    }

    private void startWithDefaults() {
        start(2, 15, 20);
    }

    private void sampleAfterMillis(long millis) {
        sampleAfterNanos(TimeUnit.MILLISECONDS.toNanos(millis));
    }

    private void sampleAfterNanos(long nanos) {
        this.nowNanos += nanos;
        this.sample.run();
    }

    /** Takes the first sample one tick after start, then a second one {@code nanos} later. */
    private void measureWindowOfNanos(long nanos) {
        sampleAfterMillis(ONE_TICK_MILLIS);
        sampleAfterNanos(nanos);
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

    // 40 ticks at interval 2: 20.5 TPS is reached at 1_951_219_512.2 ns, 14.5 TPS at 2_758_620_689.7 ns.

    @Test
    @DisplayName("A rate just below 20.5 TPS rounds to 20 and stays within tps.max")
    void justBelowUpperRoundingBoundaryStaysInRange() {
        startWithDefaults();

        measureWindowOfNanos(1_951_220_000L);

        assertThat(this.checkTPS.getTps()).as("about 20.49999 TPS rounds down").isEqualTo(20);
        assertThat(this.checkTPS.isTpsOk()).as("20 TPS is within tps.max 20").isTrue();
    }

    @Test
    @DisplayName("A rate just above 20.5 TPS rounds to 21 and exceeds tps.max")
    void justAboveUpperRoundingBoundaryExceedsMaximum() {
        startWithDefaults();

        measureWindowOfNanos(1_951_219_000L);

        assertThat(this.checkTPS.getTps()).as("about 20.50001 TPS rounds up").isEqualTo(21);
        assertThat(this.checkTPS.isTpsOk()).as("21 TPS is above tps.max 20").isFalse();
    }

    @Test
    @DisplayName("A rate just above 14.5 TPS rounds to 15 and stays within tps.min")
    void justAboveLowerRoundingBoundaryStaysInRange() {
        startWithDefaults();

        measureWindowOfNanos(2_758_620_000L);

        assertThat(this.checkTPS.getTps()).as("about 14.50000 TPS rounds up").isEqualTo(15);
        assertThat(this.checkTPS.isTpsOk()).as("15 TPS is within tps.min 15").isTrue();
    }

    @Test
    @DisplayName("A rate just below 14.5 TPS rounds to 14 and falls below tps.min")
    void justBelowLowerRoundingBoundaryFallsBelowMinimum() {
        startWithDefaults();

        measureWindowOfNanos(2_758_621_000L);

        assertThat(this.checkTPS.getTps()).as("about 14.49999 TPS rounds down").isEqualTo(14);
        assertThat(this.checkTPS.isTpsOk()).as("14 TPS is below tps.min 15").isFalse();
    }

    @Test
    @DisplayName("A server catching up after a lag spike exceeds tps.max and pauses detection")
    void catchUpAfterLagSpikePausesDetection() {
        startWithDefaults();

        measureWindowOfMillis(1000);

        assertThat(this.checkTPS.getTps()).as("40 ticks within 1000 ms is 40 TPS").isEqualTo(40);
        assertThat(this.checkTPS.isTpsOk()).as("a catch-up window above tps.max pauses detection").isFalse();
    }

    @Test
    @DisplayName("Detection resumes with the next normal window after a catch-up window")
    void detectionResumesAfterCatchUpWindow() {
        startWithDefaults();
        measureWindowOfMillis(1000);

        sampleAfterMillis(2000);

        assertThat(this.checkTPS.getTps()).as("the next normal window measures 20 TPS").isEqualTo(20);
        assertThat(this.checkTPS.isTpsOk()).as("detection must recover after one normal window").isTrue();
    }

    @Test
    @DisplayName("An interval of one second measures 20 TPS for a nominal window")
    void intervalOfOneSecondMeasuresNominalRate() {
        start(1, 15, 20);

        measureWindowOfMillis(1000);

        assertThat(this.checkTPS.getTps()).as("20 ticks within 1000 ms is 20 TPS").isEqualTo(20);
        assertThat(this.checkTPS.isTpsOk()).as("a nominal window keeps detection running").isTrue();
    }

    @Test
    @DisplayName("An interval of five seconds measures 20 TPS for a nominal window")
    void intervalOfFiveSecondsMeasuresNominalRate() {
        start(5, 15, 20);

        measureWindowOfMillis(5000);

        assertThat(this.checkTPS.getTps()).as("100 ticks within 5000 ms is 20 TPS").isEqualTo(20);
        assertThat(this.checkTPS.isTpsOk()).as("a nominal window keeps detection running").isTrue();
    }

    @Test
    @DisplayName("A sample without elapsed time keeps the previous measurement")
    void zeroElapsedTimeKeepsPreviousState() {
        startWithDefaults();
        measureWindowOfMillis(3000);

        assertThatCode(() -> sampleAfterNanos(0)).as("a zero window must not throw").doesNotThrowAnyException();

        assertThat(this.checkTPS.getTps()).as("the previous 13 TPS stays").isEqualTo(13);
        assertThat(this.checkTPS.isTpsOk()).as("the previous paused state stays").isFalse();
    }

    @Test
    @DisplayName("A sample with negative elapsed time keeps the previous measurement")
    void negativeElapsedTimeKeepsPreviousState() {
        startWithDefaults();
        measureWindowOfMillis(3000);

        assertThatCode(() -> sampleAfterMillis(-500)).as("a negative window must not throw").doesNotThrowAnyException();

        assertThat(this.checkTPS.getTps()).as("the previous 13 TPS stays").isEqualTo(13);
        assertThat(this.checkTPS.isTpsOk()).as("the previous paused state stays").isFalse();
    }

    @Test
    @DisplayName("Both bounds negative schedules no TPS check")
    void bothBoundsNegativeSchedulesNothing() {
        create(2, -1, -1);

        this.checkTPS.startCheck();

        verifyNoInteractions(this.scheduler);
    }

    @Test
    @DisplayName("The TPS check is scheduled every 20 times tps.interval ticks")
    void checkIsScheduledEveryIntervalInTicks() {
        create(5, 15, 20);

        this.checkTPS.startCheck();

        verify(this.scheduler).scheduleRepeatingTask(any(Runnable.class), eq(1L), eq(100L));
    }

    @Test
    @DisplayName("A single negative bound still schedules the TPS check")
    void singleNegativeBoundStillSchedules() {
        create(2, -1, 20);

        this.checkTPS.startCheck();

        verify(this.scheduler).scheduleRepeatingTask(any(Runnable.class), eq(1L), eq(40L));
    }
}
