package net.onelitefeather.antiredstoneclockremastered.utils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.SchedulerService;

@Singleton
public final class CheckTPS {

    private long lastPoll = System.currentTimeMillis();
    private boolean tpsOk = true;
    private long tps = 20;
    private final SchedulerService scheduler;
    private final int interval;
    private final int maximimumTPS;
    private final int minimumTPS;

    @Inject
    public CheckTPS(AntiRedstoneClockRemastered plugin, SchedulerService scheduler) {
        this.scheduler = scheduler;
        this.interval = plugin.getConfig().getInt("tps.interval", 2);
        this.maximimumTPS = plugin.getConfig().getInt("tps.max", 20);
        this.minimumTPS = plugin.getConfig().getInt("tps.min", 15);
    }

    public void startCheck() {
        if (this.minimumTPS > 0 || this.maximimumTPS > 0) {
            this.scheduler.scheduleRepeatingTask(this::runCheck, 1, 20L * this.interval);
        }
    }

    private void runCheck() {
        var current = System.currentTimeMillis();
        var spendTime = (current - this.lastPoll) / 1000;
        if (spendTime <= 0) spendTime = 1;
        this.tps = (20L * this.interval / spendTime);
        if (this.minimumTPS < 0) {
            this.tpsOk = (this.tps <= this.maximimumTPS);
        } else if (this.maximimumTPS < 0) {
            this.tpsOk = (this.tps >= this.minimumTPS);
        } else {
            this.tpsOk = (this.tps >= this.minimumTPS && this.tps <= this.maximimumTPS);
        }
        this.lastPoll = current;
    }

    public boolean isTpsOk() {
        return this.tpsOk;
    }

    public long getTps() {
        return this.tps;
    }
}
