package net.onelitefeather.antiredstoneclockremastered.service.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.SchedulerService;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class BukkitSchedulerService implements SchedulerService {

    private final AntiRedstoneClockRemastered plugin;

    public BukkitSchedulerService(AntiRedstoneClockRemastered plugin) {
        this.plugin = plugin;
    }

    @Override
    public void scheduleTask(Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public void scheduleRepeatingTask(Runnable task, long delay, long period) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    @Override
    public ScheduledTask runTaskTimerAsynchronously(Consumer<ScheduledTask> task, long delay, long period) {
        return plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, task, delay, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancelTasks() {
        plugin.getServer().getScheduler().cancelTasks(plugin);
    }
}
