package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.api.WorldGuardSupport;
import net.onelitefeather.antiredstoneclockremastered.plotsquared.v6.PlotSquaredLegacySupport;
import net.onelitefeather.antiredstoneclockremastered.plotsquared.v7.PlotSquaredModernSupport;
import net.onelitefeather.antiredstoneclockremastered.worldguard.v6.WorldGuardLegacySupport;
import net.onelitefeather.antiredstoneclockremastered.worldguard.v7.WorldGuardModernSupport;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice module for external plugin support dependencies.
 *
 * @author TheMeinerLP
 * @since 1.0.0
 * @version 1.0.0
 */
public class ExternalSupportModule extends AbstractModule {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalSupportModule.class);
    private final AntiRedstoneClockRemastered plugin;
    
    public ExternalSupportModule(AntiRedstoneClockRemastered plugin) {
        this.plugin = plugin;
    }
    
    @Override
    protected void configure() {
        // External support bindings are provided through @Provides methods
    }
    
    @Provides
    @Singleton
    @Nullable
    public WorldGuardSupport provideWorldGuardSupport() {
        Plugin wgPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (wgPlugin == null) {
            LOGGER.warn("WorldGuard hasn't been found!");
            return null;
        }
        
        @SuppressWarnings("deprecation")
        int wgVersion = Integer.parseInt(wgPlugin.getDescription().getVersion().split("\\.")[0]);
        
        WorldGuardSupport support;
        if (wgVersion > 6) {
            support = new WorldGuardModernSupport(plugin);
        } else {
            support = new WorldGuardLegacySupport(plugin);
        }
        
        if (support.registerFlag()) {
            LOGGER.info("Flag redstoneclock registered");
        } else {
            LOGGER.error("An error occurred while registering redstoneclock flag");
        }
        
        return support;
    }
    
    @Provides
    @Singleton
    @Nullable
    public PlotsquaredSupport providePlotsquaredSupport() {
        Plugin psPlugin = plugin.getServer().getPluginManager().getPlugin("PlotSquared");
        if (psPlugin == null) {
            LOGGER.warn("PlotSquared hasn't been found!");
            return null;
        }
        
        @SuppressWarnings("deprecation")
        int psVersion = Integer.parseInt(psPlugin.getDescription().getVersion().split("\\.")[0]);
        
        if (psVersion < 6) {
            LOGGER.warn("We don't support PS5 currently also you use a unsupported version of PlotSquared!!!");
            return null;
        }
        
        PlotsquaredSupport support;
        if (psVersion < 7) {
            LOGGER.warn("You use a legacy version of PlotSquared!");
            support = new PlotSquaredLegacySupport();
        } else {
            LOGGER.info("Thanks to hold your software up-to date <3");
            support = new PlotSquaredModernSupport();
        }
        
        support.init();
        return support;
    }
}