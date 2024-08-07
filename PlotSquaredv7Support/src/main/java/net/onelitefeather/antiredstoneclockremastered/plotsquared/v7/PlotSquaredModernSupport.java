package net.onelitefeather.antiredstoneclockremastered.plotsquared.v7;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import net.onelitefeather.antiredstoneclockremastered.api.AbstractPlotsquaredSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class PlotSquaredModernSupport extends AbstractPlotsquaredSupport {
    @Override
    public void init() {
        GlobalFlagContainer.getInstance().addFlag(RedstoneClockFlag.REDSTONE_CLOCK_FALSE);
    }

    @Override
    public boolean isAllowedPlot(@NotNull Location location) {
        var plotArea = PlotSquared.get().getPlotAreaManager().getPlotArea(BukkitUtil.adapt(location));
        if (plotArea == null) return false;
        Plot plot = plotArea.getPlot(BukkitUtil.adapt(location));
        if (plot == null) return false;
        return plot.getFlag(RedstoneClockFlag.class);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getVersion() {
        Plugin plotSquaredPlugin = Bukkit.getPluginManager().getPlugin("PlotSquared");
        if (plotSquaredPlugin == null) {
            return "unknown";
        }
        return plotSquaredPlugin.getDescription().getVersion();
    }
}
