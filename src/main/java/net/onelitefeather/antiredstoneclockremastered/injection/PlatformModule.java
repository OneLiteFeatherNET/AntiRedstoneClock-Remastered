package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.service.api.SchedulerService;
import net.onelitefeather.antiredstoneclockremastered.service.factory.RegionServiceFactory;
import net.onelitefeather.antiredstoneclockremastered.service.factory.SchedulerServiceFactory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Guice module for platform-specific dependencies.
 *
 * <p>
 *     This module handles bindings and providers that differ between Folia and Paper platforms.
 * </p>
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
        bind(AntiRedstoneClockRemastered.class).toInstance(plugin);
        bind(Plugin.class).toInstance(plugin);
        bind(JavaPlugin.class).toInstance(plugin);
    }
    
    /**
     * Example of how platform-specific services could be provided
     * This would allow for different implementations on Folia vs Paper
     */
    @Provides
    @Singleton
    public SchedulerService provideSchedulerService(AntiRedstoneClockRemastered plugin) {
        return SchedulerServiceFactory.createService(plugin);
    }
    
    @Provides
    @Singleton  
    public RegionService provideRegionService(AntiRedstoneClockRemastered plugin) {
        return RegionServiceFactory.createService(plugin);
    }
}