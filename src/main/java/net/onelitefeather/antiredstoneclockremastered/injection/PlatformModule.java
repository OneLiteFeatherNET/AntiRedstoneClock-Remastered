package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Example module showing how to add Folia-specific implementations.
 * This demonstrates the extensibility benefits of the DI refactoring.
 *
 * @author TheMeinerLP
 * @since 2.2.0
 * @version 1.0.0
 */
public final class PlatformModule extends AbstractModule {
    
    private final AntiRedstoneClockRemastered plugin;
    
    public PlatformModule(AntiRedstoneClockRemastered plugin) {
        this.plugin = plugin;
    }
    
    @Override
    protected void configure() {
        // Platform-specific bindings can be configured here
    }
    
    /**
     * Example of how platform-specific services could be provided
     * This would allow for different implementations on Folia vs Paper
     */
    @Provides
    @Singleton
    public SchedulerService provideSchedulerService() {
        // Check if running on Folia
        if (isFolia()) {
            return new FoliaSchedulerService(plugin);
        } else {
            return new BukkitSchedulerService(plugin);
        }
    }
    
    @Provides
    @Singleton  
    public RegionService provideRegionService() {
        // Folia has different region handling requirements
        if (isFolia()) {
            return new FoliaRegionService(plugin);
        } else {
            return new DefaultRegionService(plugin);
        }
    }
    
    /**
     * Detect if running on Folia
     */
    private boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    // Example service interfaces that would need to be created
    
    /**
     * Abstraction for scheduling tasks
     * Allows different implementations for Folia's threaded regions
     */
    public interface SchedulerService {
        void scheduleTask(Runnable task);
        void scheduleRepeatingTask(Runnable task, long delay, long period);
        ScheduledTask runTaskTimerAsynchronously(Consumer<ScheduledTask> task, long delay, long period);
        void cancelTasks();
    }
    
    /**
     * Abstraction for region-based operations
     * Folia requires region-aware task scheduling
     */
    public interface RegionService {
        void executeInRegion(Location location, Runnable task);
        boolean isRegionOwner(Location location);
    }
    
    // Example implementations would be created in separate classes
    private static class FoliaSchedulerService implements SchedulerService {
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
            return this.plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, task, delay, period, TimeUnit.MILLISECONDS);
        }

        @Override
        public void cancelTasks() {
            this.plugin.getServer().getScheduler().cancelTasks(plugin);
        }
    }
    
    private static class BukkitSchedulerService implements SchedulerService {
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
    
    private static class FoliaRegionService implements RegionService {
        private final AntiRedstoneClockRemastered plugin;
        
        public FoliaRegionService(AntiRedstoneClockRemastered plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public void executeInRegion(Location location, Runnable task) {
            this.plugin.getServer().getRegionScheduler().execute(this.plugin,location, task);
        }
        
        @Override
        public boolean isRegionOwner(Location location) {
            return this.plugin.getServer().isOwnedByCurrentRegion(location);
        }
    }
    
    private static class DefaultRegionService implements RegionService {
        private final AntiRedstoneClockRemastered plugin;
        
        public DefaultRegionService(AntiRedstoneClockRemastered plugin) {
            this.plugin = plugin;
        }
        
        @Override
        public void executeInRegion(Location location, Runnable task) {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, task, 1);
        }
        
        @Override
        public boolean isRegionOwner(Location location) {
            // Always true for non-Folia
            return true;
        }
    }
}