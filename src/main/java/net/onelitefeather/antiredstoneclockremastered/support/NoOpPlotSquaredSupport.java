package net.onelitefeather.antiredstoneclockremastered.support;

import net.onelitefeather.antiredstoneclockremastered.api.AbstractPlotsquaredSupport;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class NoOpPlotSquaredSupport extends AbstractPlotsquaredSupport {
    @Override
    public void init() {
    }

    @Override
    public boolean isAllowedPlot(@NotNull Location location) {
        return false;
    }

    @Override
    public String getVersion() {
        return "No PlotSquared found";
    }
}
