package net.onelitefeather.antiredstoneclockremastered.service.region;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import org.bukkit.Location;

public final class BukkitRegionService implements RegionService {

    private final AntiRedstoneClockRemastered plugin;

    public BukkitRegionService(AntiRedstoneClockRemastered plugin) {
        this.plugin = plugin;
    }

    @Override
    public void executeInRegion(Location location, Runnable task) {
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, task, 1);
    }

    @Override
    public void executeInRegion(Location location, Runnable task, long delay) {
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, task, delay);
    }

    @Override
    public boolean isRegionOwner(Location location) {
        // Always true for non-Folia
        return true;
    }
}
