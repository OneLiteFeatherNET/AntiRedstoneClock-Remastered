package net.onelitefeather.antiredstoneclockremastered.plotsquared.v4;

import com.github.intellectualsites.plotsquared.api.PlotAPI;
import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import net.onelitefeather.antiredstoneclockremastered.api.AbstractPlotsquaredSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class PlotSquaredWhatTheHellSupport extends AbstractPlotsquaredSupport {

    private static final PlotAPI PLOT_API = new PlotAPI();
    public static final String FLAG_STRING = "redstone-clock";
    @Override
    public void init() {
        PLOT_API.addFlag(new RedstoneClockFlag());
    }

    @Override
    public boolean isAllowedPlot(@NotNull Location location) {
        var plot = BukkitUtil.getPlot(location);
        if (plot != null) {
            var flagOptional = plot.getFlag(Flags.getFlag(FLAG_STRING));
            if (flagOptional.isPresent()) {
                var flag = flagOptional.get();
                if (flag instanceof RedstoneClockFlag redstoneClockFlag) {
                    return redstoneClockFlag.isFalse(plot);
                }
            }
        }

        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getVersion() {
        var plotSquaredPlugin = Bukkit.getPluginManager().getPlugin("PlotSquared");
        if (plotSquaredPlugin == null) {
            return "unkown";
        }
        return plotSquaredPlugin.getDescription().getVersion();
    }
}
