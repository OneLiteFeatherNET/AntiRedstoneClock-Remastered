package net.onelitefeather.antiredstoneclockremastered.plotsquared.v6;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import net.onelitefeather.antiredstoneclockremastered.api.AbstractPlotsquaredSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class PlotSquaredLegacySupport extends AbstractPlotsquaredSupport {
    @Override
    public void init() {
        GlobalFlagContainer.getInstance().addFlag(new RedstoneClockFlag(false));
    }

    @Override
    public boolean isAllowedPlot(@NotNull Location location) {
        var plotArea = PlotSquared.get().getPlotAreaManager().getPlotArea(BukkitUtil.adapt(location));
        if (plotArea != null) {
            var flag = plotArea.getFlagContainer().getFlag(RedstoneClockFlag.class);
            if (flag != null) {
                return !flag.getValue();
            }
        }
        return false;
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
