package net.onelitefeather.antiredstoneclockremastered.service.factory;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.BukkitRedstoneClockService;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating RedstoneClockService implementations.
 * This factory determines the appropriate implementation based on the server platform.
 */
public final class RedstoneClockServiceFactory {

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
    public static RedstoneClockService createService(@NotNull AntiRedstoneClockRemastered plugin) {
        // For now, only Bukkit implementation is available
        // Future implementations (e.g., Folia) can be added here based on platform detection
        return new BukkitRedstoneClockService(plugin);
    }

    /**
     * Detects if the server is running on Folia.
     * This method can be extended in the future to detect Folia and return a FoliaRedstoneClockService.
     *
     * @return true if Folia is detected, false otherwise
     */
    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}