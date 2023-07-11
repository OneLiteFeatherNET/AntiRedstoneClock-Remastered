package net.onelitefeather.antiredstoneclockremastered.utils;

import org.bukkit.plugin.Plugin;

public final class CheckTPS {

    private long lastPoll = System.currentTimeMillis();
    private boolean tpsOk = true;
    private long tps = 20;
    private final Plugin plugin;
    private final int interval;
    private final int maximimumTPS;
    private final int minimumTPS;

    public CheckTPS(Plugin plugin, int interval, int maximimumTPS, int minimumTPS) {
        this.plugin = plugin;
        this.interval = interval;
        this.maximimumTPS = maximimumTPS;
        this.minimumTPS = minimumTPS;
    }

    public void startCheck() {
        if (this.minimumTPS > 0 || this.maximimumTPS > 0) {
            this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, this::runCheck, 0, 20L * this.interval);
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
