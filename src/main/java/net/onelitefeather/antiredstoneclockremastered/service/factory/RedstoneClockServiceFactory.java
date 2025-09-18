package net.onelitefeather.antiredstoneclockremastered.service.factory;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.BukkitRedstoneClockService;
// import net.onelitefeather.antiredstoneclockremastered.service.impl.FoliaRedstoneClockService;
import io.papermc.paper.ServerBuildInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating RedstoneClockService implementations.
 * This factory determines the appropriate implementation based on the server platform.
 *
 * @author OneLiteFeather
 * @version 1.0.0
 * @since 1.0.0
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
        if (isFolia()) {
            plugin.getLogger().info("Folia detected - using FoliaRedstoneClockService");
            // Uncomment when ready to enable Folia support:
            // return new FoliaRedstoneClockService(plugin);
            plugin.getLogger().warning("Folia implementation not yet ready, falling back to Bukkit implementation");
        }
        
        plugin.getLogger().info("Using BukkitRedstoneClockService");
        return new BukkitRedstoneClockService(plugin);
    }

    /**
     * Detects if the server is running on Folia.
     * Folia support is available starting from Paper version 1.20 and above.
     *
     * @return true if Folia is detected, false otherwise
     */
    private static boolean isFolia() {
        try {
            ServerBuildInfo buildInfo = ServerBuildInfo.buildInfo();
            String version = buildInfo.minecraftVersionId();
            
            // Check if running Paper 1.20+ which supports Folia
            if (version.startsWith("1.20") || version.startsWith("1.21") || version.startsWith("1.22")) {
                // Additional check for Folia-specific classes
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                return true;
            }
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}