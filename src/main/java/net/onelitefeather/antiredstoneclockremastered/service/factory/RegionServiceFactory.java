package net.onelitefeather.antiredstoneclockremastered.service.factory;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.BukkitRegionService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.FoliaRegionService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating instances of RegionService based on the server environment.
 *
 * @since 2.3.0-beta.10
 * @version 1.0.0
 * @author TheMeinerLP
 */
public final class RegionServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegionServiceFactory.class);

    private RegionServiceFactory() {
        // Utility class
    }


    /**
     * Creates the appropriate RegionService implementation for the current platform.
     *
     * @param plugin the main plugin instance
     * @return the appropriate RegionService implementation
     */
    @NotNull
    public static RegionService createService(@NotNull AntiRedstoneClockRemastered plugin) {
        if (FoliaHelper.isFolia()) {
            LOGGER.info("Folia detected - using FoliaRegionService");
            return new FoliaRegionService(plugin);
        }

        LOGGER.info("Using BukkitRegionService");
        return new BukkitRegionService(plugin);
    }
}
