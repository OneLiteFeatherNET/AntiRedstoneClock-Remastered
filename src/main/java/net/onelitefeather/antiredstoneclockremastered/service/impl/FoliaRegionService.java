package net.onelitefeather.antiredstoneclockremastered.service.impl;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class FoliaRegionService implements RegionService {

    private final AntiRedstoneClockRemastered plugin;

    public FoliaRegionService(AntiRedstoneClockRemastered plugin) {
        this.plugin = plugin;
    }

    @Override
    public void executeInRegion(@NotNull Location location, @NotNull Runnable task) {
        this.plugin.getServer().getRegionScheduler().execute(this.plugin,location, task);
    }

    @Override
    public void executeInRegion(@NotNull Location location, @NotNull Runnable task, long delay) {
        this.plugin.getServer().getRegionScheduler().runDelayed(this.plugin, location, scheduledTask -> task.run(), delay);
    }

    @Override
    public boolean isRegionOwner(@NotNull Location location) {
        return this.plugin.getServer().isOwnedByCurrentRegion(location);
    }
}
