package net.onelitefeather.antiredstoneclockremastered.worldguard.v6;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.protection.DelayedRegionOverlapAssociation;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.onelitefeather.antiredstoneclockremastered.api.AbstractWorldGuardSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class WorldGuardLegacySupport extends AbstractWorldGuardSupport {

    private static final StateFlag REDSTONECLOCK_FLAG = new StateFlag("redstone-clock", false);

    private static final WorldGuardPlugin WORLD_GUARD_PLUGIN = loadPlugin();

    public WorldGuardLegacySupport(@NotNull Plugin plugin) {
        super(plugin);
    }

    private static WorldGuardPlugin loadPlugin() {

        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may be loaded
        if (plugin instanceof WorldGuardPlugin wgp) return wgp;
        return null;
    }

    @Override
    public boolean isRegionAllowed(@NotNull Location location) {
        boolean result = false;
        if (WORLD_GUARD_PLUGIN != null) {
            RegionQuery query = WORLD_GUARD_PLUGIN.getRegionContainer().createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(location);
            RegionAssociable associable = new DelayedRegionOverlapAssociation(query, location);
            if (set.testState(associable, REDSTONECLOCK_FLAG)) {
                result = true;
            } else {
                RegionManager regionManager = WORLD_GUARD_PLUGIN.getRegionManager(location.getWorld());
                result = checkRegionFromConfigFile(location, regionManager);
            }
        }
        return result;
    }

    private boolean checkRegionFromConfigFile(@NotNull Location loc, RegionManager regionManager) {
        if (regionManager != null) {
            ApplicableRegionSet regions = getRegion(regionManager, loc);
            for (String ignoreRegion : this.plugin.getConfig().getStringList(configPath)) {
                for (ProtectedRegion region : regions.getRegions()) {
                    if (region.getId().equals(ignoreRegion)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ApplicableRegionSet getRegion(RegionManager regionManager, Location loc) {
        Vector vector = new Vector(loc.getX(), loc.getY(), loc.getZ());
        return regionManager.getApplicableRegions(vector);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getVersion() {
        if (WORLD_GUARD_PLUGIN == null) {
            return "undefined";
        } else {
            return WORLD_GUARD_PLUGIN.getDescription().getVersion().substring(0, 1);
        }
    }

    @Override
    public boolean registerFlag() {
        boolean flagLoaded = false;
        if (WORLD_GUARD_PLUGIN != null) {
            FlagRegistry registry = WORLD_GUARD_PLUGIN.getFlagRegistry();
            try {
                // register our flag with the registry
                registry.register(REDSTONECLOCK_FLAG);
                flagLoaded = true;
            } catch (FlagConflictException e) {
                Bukkit.getLogger().severe("A plugin already use the flag redstoneclock. WorldGuard flag support will not work");
            }
        }
        return flagLoaded;
    }
}
