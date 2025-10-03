package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.api.WorldGuardSupport;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class MetricsModule {
    private final Metrics metrics;
    private final PlotsquaredSupport plotsquaredSupport;
    private final WorldGuardSupport worldGuardSupport;
    private final JavaPlugin plugin;

    @Inject
    public MetricsModule(JavaPlugin plugin, PlotsquaredSupport plotsquaredSupport, WorldGuardSupport worldGuardSupport) {
        this.plugin = plugin;
        this.plotsquaredSupport = plotsquaredSupport;
        this.worldGuardSupport = worldGuardSupport;
        this.metrics = new Metrics(plugin, 19085);
    }

    public void registerCharts() {
        metrics.addCustomChart(new SimplePie("worldguard", this::bstatsWorldGuardVersion));
        metrics.addCustomChart(new SimplePie("plotsquared", this::bstatsPlotSquaredVersion));
        metrics.addCustomChart(new DrilldownPie("maxcount", this::bstatsMaxCount));
    }

    private Map<String, Map<String, Integer>> bstatsMaxCount() {
        var map = new HashMap<String, Map<String, Integer>>();
        var count = plugin.getConfig().getInt("clock.maxCount");
        var entry = Map.of(String.valueOf(count), 1);
        switch (count) {
            case 0 -> map.put("0 \uD83D\uDEAB", entry);
            case 1, 2, 3, 4, 5 -> map.put("1-5 \uD83D\uDE10", entry);
            case 6, 7, 8, 9, 10 -> map.put("6-10 \uD83D\uDE42", entry);
            case 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 -> map.put("11-25 \uD83D\uDE0A", entry);
            case 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50 ->
                    map.put("26-50 \uD83D\uDE00", entry);
            default -> map.put("50+ \uD83D\uDE01", entry);
        }
        return map;
    }

    private String bstatsPlotSquaredVersion() {
        return plotsquaredSupport.getVersion();
    }

    private String bstatsWorldGuardVersion() {
        return worldGuardSupport.getVersion();
    }
}

