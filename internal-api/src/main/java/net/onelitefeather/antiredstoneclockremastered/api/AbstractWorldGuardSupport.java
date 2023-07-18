package net.onelitefeather.antiredstoneclockremastered.api;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * The abstract Worldguard Support class which implements the WorldguardSupport interface to get Plugin and Version
 * The configpath is always the same for the ignored regions, used to check if redstone clocks are in not ignored regions
 */
public abstract non-sealed class AbstractWorldGuardSupport implements WorldGuardSupport {

    protected final @NotNull Plugin plugin;
    protected final @NotNull String configPath = "check.ignoredRegions";

    protected AbstractWorldGuardSupport(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }
}
