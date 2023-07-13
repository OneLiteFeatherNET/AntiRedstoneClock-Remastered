package net.onelitefeather.antiredstoneclockremastered.api;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public sealed interface PlotsquaredSupport permits AbstractPlotsquaredSupport {

    void init();

    boolean isAllowedPlot(@NotNull Location location);

    String getVersion();

}
