package net.onelitefeather.antiredstoneclockremastered.worldguard.v7;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitWorldGuardPlatform;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.DelayedRegionOverlapAssociation;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.onelitefeather.antiredstoneclockremastered.api.AbstractWorldGuardSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class WorldGuardModernSupport extends AbstractWorldGuardSupport {

    private static final WorldGuardPlugin WORLD_GUARD_PLUGIN = loadPlugin();

    private static final StateFlag REDSTONECLOCK_FLAG = new StateFlag("redstone-clock", false);

    public WorldGuardModernSupport(@NotNull Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isRegionAllowed(@NotNull Location location) {
        boolean result = false;
        if (WORLD_GUARD_PLUGIN != null) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
            RegionAssociable associable = new DelayedRegionOverlapAssociation(query, BukkitAdapter.adapt(location));
            if (set.testState(associable, REDSTONECLOCK_FLAG)) {
                return true;
            } else {
                RegionManager regionManager = getRegionManager(location.getWorld());
                result = checkRegionFromConfigFile(location, regionManager);
            }
        }
        return result;
    }

    private static WorldGuardPlugin loadPlugin() {

        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may be loaded
        if (plugin instanceof WorldGuardPlugin wgp) return wgp;
        return null;
    }

    private boolean checkRegionFromConfigFile(@NotNull Location location, RegionManager regionManager) {
        if (regionManager != null) {
            ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());
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

    private RegionManager getRegionManager(World world) {
        BukkitWorldGuardPlatform wgPlatform = (BukkitWorldGuardPlatform) WorldGuard.getInstance().getPlatform();
        com.sk89q.worldedit.world.World worldEditWorld = wgPlatform.getMatcher().getWorldByName(world.getName());
        return wgPlatform.getRegionContainer().get(worldEditWorld);
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
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(REDSTONECLOCK_FLAG);
            flagLoaded = true;
        } catch (FlagConflictException e) {
            Bukkit.getLogger().severe("A plugin already use the flag redstone-clock. WorldGuard flag support will not work");
        }
        return flagLoaded;
    }
}
