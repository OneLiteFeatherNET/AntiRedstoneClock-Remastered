package net.onelitefeather.antiredstoneclockremastered.plotsquared.v6;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import net.onelitefeather.antiredstoneclockremastered.api.AbstractPlotsquaredSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Plotsquared legacy is no longer supported by the IntellectualSites team. Take sure to update Plotsquared, FAWE and
 * your minecraft version to prevent not fixed issues and exploits.
 * This class creates a new third party Plotsquared flag for enabling / disabling antiredstone detection on a plot.
 * It also gets the accurate version of Plotsquared and checks if the location of the redstone clock is on a plot.
 */
public final class PlotSquaredLegacySupport extends AbstractPlotsquaredSupport {

    /**
     * initialize a new flag with the default boolean value false; this flag disables ARC-R detection on default,
     * you have to enable this flag though the plotsquared flag command
     */
    @Override
    public void init() {
        GlobalFlagContainer.getInstance().addFlag(new RedstoneClockFlag(false));
    }

    /**
     * Checks if the location of the redstone clock is in a plotarea ("plot")
     * @param location the location of the redstone clock
     * @return true if the location is inside a plotarea and has the redstoneclockflag enabled,
     * false if it is not inside a plotarea and has the redstoneclockflag disabled
     *
     */

    @Override
    public boolean isAllowedPlot(@NotNull Location location) {
        var plotArea = PlotSquared.get().getPlotAreaManager().getPlotArea(BukkitUtil.adapt(location));
        if (plotArea != null) {
            var flag = plotArea.getFlagContainer().getFlag(RedstoneClockFlag.class);
            if (flag != null) {
                return flag.getValue();
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
