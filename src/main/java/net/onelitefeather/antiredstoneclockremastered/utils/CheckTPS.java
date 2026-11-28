package net.onelitefeather.antiredstoneclockremastered.utils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.LongSupplier;

@Singleton
public final class CheckTPS {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckTPS.class);

    private final LongSupplier nanoTime;
    private long lastPoll;
    private boolean hasBaseline;
    private boolean tpsOk = true;
    private long tps = 20;
    private final SchedulerService scheduler;
    private final int interval;
    private final int maximimumTPS;
    private final int minimumTPS;

    @Inject
    public CheckTPS(AntiRedstoneClockRemastered plugin, SchedulerService scheduler) {
        this(plugin, scheduler, System::nanoTime);
    }

    CheckTPS(AntiRedstoneClockRemastered plugin, SchedulerService scheduler, LongSupplier nanoTime) {
        this.scheduler = scheduler;
        this.nanoTime = nanoTime;
        this.interval = plugin.getConfig().getInt("tps.interval", 2);
        this.maximimumTPS = plugin.getConfig().getInt("tps.max", 20);
        this.minimumTPS = plugin.getConfig().getInt("tps.min", 15);
    }

    public void startCheck() {
        if (this.minimumTPS > 0 || this.maximimumTPS > 0) {
            LOGGER.debug("Starting TPS check every {} seconds, accepted range is {} to {}",
                    this.interval, this.minimumTPS, this.maximimumTPS);
            this.scheduler.scheduleRepeatingTask(this::runCheck, 1, 20L * this.interval);
            return;
        }
        LOGGER.debug("TPS check is disabled, both tps.min and tps.max are negative");
    }

    private void runCheck() {
        var current = this.nanoTime.getAsLong();
        if (!this.hasBaseline) {
            // The first sample only records the start of the first window; there is nothing to measure yet.
            this.lastPoll = current;
            this.hasBaseline = true;
            return;
        }
        var elapsedNanos = current - this.lastPoll;
        if (elapsedNanos <= 0) return;
        var measuredTps = 20.0 * this.interval * 1_000_000_000L / elapsedNanos;
        // Round to whole ticks so ordinary scheduling jitter around the interval does not leave the range.
        this.tps = Math.round(measuredTps);
        if (this.minimumTPS < 0) {
            this.tpsOk = (this.tps <= this.maximimumTPS);
        } else if (this.maximimumTPS < 0) {
            this.tpsOk = (this.tps >= this.minimumTPS);
        } else {
            this.tpsOk = (this.tps >= this.minimumTPS && this.tps <= this.maximimumTPS);
        }
        this.lastPoll = current;
        LOGGER.debug("Measured {} TPS ({} ms for {} ticks), clock detection is {}", this.tps,
                elapsedNanos / 1_000_000, 20L * this.interval, this.tpsOk ? "running" : "paused");
    }

    public boolean isTpsOk() {
        return this.tpsOk;
    }

    public long getTps() {
        return this.tps;
    }
}
