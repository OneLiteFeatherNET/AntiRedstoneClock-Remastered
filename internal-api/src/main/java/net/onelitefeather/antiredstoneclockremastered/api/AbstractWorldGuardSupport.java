package net.onelitefeather.antiredstoneclockremastered.api;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract non-sealed class AbstractWorldGuardSupport implements WorldGuardSupport {

    protected final @NotNull Plugin plugin;
    protected final @NotNull String configPath = "check.ignoredRegions";

    protected AbstractWorldGuardSupport(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }
}
