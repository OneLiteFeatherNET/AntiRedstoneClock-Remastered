package net.onelitefeather.antiredstoneclockremastered.api;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * The interface for Worldguard Support
 */
public sealed interface WorldGuardSupport permits AbstractWorldGuardSupport {

    boolean isRegionAllowed(@NotNull Location location);

    // This is not used yet, but is going to be used for statistics
    String getVersion();

    boolean registerFlag();

}
