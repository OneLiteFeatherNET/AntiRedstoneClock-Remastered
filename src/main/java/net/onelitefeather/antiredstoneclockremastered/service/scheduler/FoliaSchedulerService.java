package net.onelitefeather.antiredstoneclockremastered.service.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.SchedulerService;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class FoliaSchedulerService implements SchedulerService {

    private static final long MILLISECONDS_PER_TICK = 50L;

    private final AntiRedstoneClockRemastered plugin;

    public FoliaSchedulerService(AntiRedstoneClockRemastered plugin) {
        this.plugin = plugin;
    }

    @Override
    public void scheduleTask(Runnable task) {
        this.plugin.getServer().getGlobalRegionScheduler().run(plugin, t -> task.run());
    }

    @Override
    public void scheduleRepeatingTask(Runnable task, long delay, long period) {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), delay, period);
    }

    @Override
    public ScheduledTask runTaskTimerAsynchronously(Consumer<ScheduledTask> task, long delay, long period) {
        return this.plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, task, delay * MILLISECONDS_PER_TICK, period * MILLISECONDS_PER_TICK, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancelTasks() {
        this.plugin.getServer().getScheduler().cancelTasks(plugin);
    }
}
