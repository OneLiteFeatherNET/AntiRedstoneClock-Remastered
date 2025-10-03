package net.onelitefeather.antiredstoneclockremastered.service.impl;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import org.bukkit.Location;

public final class FoliaRegionService implements RegionService {

    private final AntiRedstoneClockRemastered plugin;

    public FoliaRegionService(AntiRedstoneClockRemastered plugin) {
        this.plugin = plugin;
    }

    @Override
    public void executeInRegion(Location location, Runnable task) {
        this.plugin.getServer().getRegionScheduler().execute(this.plugin,location, task);
    }

    @Override
    public boolean isRegionOwner(Location location) {
        return this.plugin.getServer().isOwnedByCurrentRegion(location);
    }
}
