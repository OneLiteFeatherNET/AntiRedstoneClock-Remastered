package net.onelitefeather.antiredstoneclockremastered.service.factory;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.NotificationService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.service.decision.BukkitDecisionService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating RedstoneClockService implementations.
 * This factory determines the appropriate implementation based on the server platform.
 *
 * @author TheMeinerLP
 * @version 2.2.0
 * @since 1.0.0
 */
public final class DecisionServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecisionServiceFactory.class);

    private DecisionServiceFactory() {
        // Utility class
    }

    /**
     * Creates the appropriate RedstoneClockService implementation for the current platform.
     *
     * @param plugin the main plugin instance
     * @return the appropriate RedstoneClockService implementation
     */
    @NotNull
    public static DecisionService createService(@NotNull AntiRedstoneClockRemastered plugin,
                                                RegionService regionService, RedstoneClockMiddleware middleware,
                                                NotificationService notificationService) {
        LOGGER.info("Using BukkitDecisionService as DecisionService implementation.");
        return new BukkitDecisionService(plugin, middleware, regionService, notificationService);
    }
}