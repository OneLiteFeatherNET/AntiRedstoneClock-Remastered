package net.onelitefeather.antiredstoneclockremastered.api;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public sealed interface WorldGuardSupport permits AbstractWorldGuardSupport {

    boolean isRegionAllowed(@NotNull Location location);

    String getVersion();

    boolean registerFlag();

}
