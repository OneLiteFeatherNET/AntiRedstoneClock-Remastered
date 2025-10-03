package net.onelitefeather.antiredstoneclockremastered.service.factory;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.SchedulerService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.BukkitSchedulerService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.FoliaSchedulerService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating instances of SchedulerService based on the server environment.
 *
 * @since 2.3.0-beta.10
 * @version 1.0.0
 * @author TheMeinerLP
 */
public final class SchedulerServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerServiceFactory.class);

    private SchedulerServiceFactory() {
        // Utility class
    }

    /**
     * Creates the appropriate SchedulerService implementation for the current platform.
     *
     * @param plugin the main plugin instance
     * @return the appropriate SchedulerService implementation
     */
    @NotNull
    public static SchedulerService createService(@NotNull AntiRedstoneClockRemastered plugin) {
        if (FoliaHelper.isFolia()) {
            LOGGER.info("Folia detected - using FoliaSchedulerService");
            return new FoliaSchedulerService(plugin);
        }

        LOGGER.info("Using BukkitSchedulerService");
        return new BukkitSchedulerService(plugin);
    }
}
