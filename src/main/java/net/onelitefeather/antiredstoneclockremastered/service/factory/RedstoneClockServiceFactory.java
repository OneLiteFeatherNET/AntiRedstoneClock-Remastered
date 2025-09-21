package net.onelitefeather.antiredstoneclockremastered.service.factory;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.api.WorldGuardSupport;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.BukkitRedstoneClockService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.onelitefeather.antiredstoneclockremastered.service.factory.FoliaHelper.isFolia;

/**
 * Factory for creating RedstoneClockService implementations.
 * This factory determines the appropriate implementation based on the server platform.
 *
 * @author TheMeinerLP
 * @version 2.2.0
 * @since 1.0.0
 */
public final class RedstoneClockServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedstoneClockServiceFactory.class);

    private RedstoneClockServiceFactory() {
        // Utility class
    }

    /**
     * Creates the appropriate RedstoneClockService implementation for the current platform.
     *
     * @param plugin the main plugin instance
     * @return the appropriate RedstoneClockService implementation
     */
    @NotNull
    public static RedstoneClockService createService(@NotNull AntiRedstoneClockRemastered plugin, RegionService regionService, PlotsquaredSupport plotsquaredSupport, WorldGuardSupport worldGuardSupport) {
        if (FoliaHelper.isFolia()) {
            LOGGER.info("Folia detected - using FoliaRedstoneClockService");
            // Uncomment when ready to enable Folia support:
            // return new FoliaRedstoneClockService(plugin);
            LOGGER.warn("Folia implementation not yet ready, falling back to Bukkit implementation");
        }
        
        LOGGER.info("Using BukkitRedstoneClockService");
        return new BukkitRedstoneClockService(plugin, regionService, worldGuardSupport, plotsquaredSupport);
    }
}