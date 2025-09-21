package net.onelitefeather.antiredstoneclockremastered.service.factory;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.injection.PlatformModule;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.BukkitRedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.BukkitRegionService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.FoliaRegionService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.onelitefeather.antiredstoneclockremastered.service.factory.FoliaHelper.isFolia;

/**
 * Factory for creating instances of RegionService based on the server environment.
 *
 * @since 2.3.0-beta.10
 * @version 1.0.0
 * @author TheMeinerLP
 */
public class RegionServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegionServiceFactory.class);

    private RegionServiceFactory() {
        // Utility class
    }


    /**
     * Creates the appropriate RedstoneClockService implementation for the current platform.
     *
     * @param plugin the main plugin instance
     * @return the appropriate RedstoneClockService implementation
     */
    @NotNull
    public static RegionService createService(@NotNull AntiRedstoneClockRemastered plugin) {
        if (isFolia()) {
            LOGGER.info("Folia detected - using FoliaRegionService");
            return new FoliaRegionService(plugin);
        }

        LOGGER.info("Using BukkitRegionService");
        return new BukkitRegionService(plugin);
    }
}
