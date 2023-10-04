package net.onelitefeather.antiredstoneclockremastered.plotsquared.v4;

import com.github.intellectualsites.plotsquared.api.PlotAPI;
import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import net.onelitefeather.antiredstoneclockremastered.api.AbstractPlotsquaredSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * This class is for Plotsquared v4 support and is called "PlotSquaredWhatTheHellSupport" because it is legacy and
 * Plotsquared v4 is no longer supported by the IntellectualSites team. Take sure to update Plotsquared, FAWE and
 * your minecraft version to prevent not fixed issues and exploits.
 * This class creates a new third party Plotsquared flag for enabling / disabling antiredstone detection on a plot.
 * It also gets the accurate version of Plotsquared and checks if the location of the redstone clock is on a plot.
 */

public final class PlotSquaredWhatTheHellSupport extends AbstractPlotsquaredSupport {

    private static final PlotAPI PLOT_API = new PlotAPI();
    public static final String FLAG_STRING = "redstone-clock";
    @Override
    public void init() {
        PLOT_API.addFlag(new RedstoneClockFlag());
    }

    /**
     * Checks if the location of the redstone clock is in a plot
     * @param location the location of the redstone clock
     * @return true if the location is inside a plot and has the redstoneclockflag enabled,
     * false if it is not inside a plot and has the redstoneclockflag disabled
     */
    @Override
    public boolean isAllowedPlot(@NotNull Location location) {
        var plot = BukkitUtil.getPlot(location);
        if (plot != null) {
            var flagOptional = plot.getFlag(Flags.getFlag(FLAG_STRING));
            if (flagOptional.isPresent()) {
                var flag = flagOptional.get();
                if (flag instanceof RedstoneClockFlag redstoneClockFlag) {
                    return redstoneClockFlag.isTrue(plot);
                }
            }
        }

        return false;
    }

    /** This method is deprecated and checks the Plotsquared version to garantee matching support
     *
     * @return the plotsquared version of the Plotsquared plugin, if it is enabled, if not, return unknown
     */

    @Override
    @SuppressWarnings("deprecation")
    public String getVersion() {
        var plotSquaredPlugin = Bukkit.getPluginManager().getPlugin("PlotSquared");
        if (plotSquaredPlugin == null) {
            return "unknown";
        }
        return plotSquaredPlugin.getDescription().getVersion();
    }
}
