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

/**
 * The implementation of WorldGuardSupport class, looking for Worldguard in an enabled state
 * and in addition check if the worldguard region(s) is/are allowed
 * to register a redstone-clock flag which is default disabled.
 * Also check if the redstone-clock flag can be applied to overlapping worldguard region(s)
 */
public final class WorldGuardModernSupport extends AbstractWorldGuardSupport {

    private static final WorldGuardPlugin WORLD_GUARD_PLUGIN = loadPlugin();

    private static final StateFlag REDSTONECLOCK_FLAG = new StateFlag("redstone-clock", false);

    public WorldGuardModernSupport(@NotNull Plugin plugin) {
        super(plugin);
    }

    /**
     * The actual check if the worldguard regions are allowed to add a redstone-clock flag, considering
     * overlapping regions
     * @param location the location of the redstone-clock
     * @return the boolean result whether the region is allowed or not
     */
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

    /**
     * Searches for enabled Worldguard
     * @return null if Worldguard was not found or is not enabled
     */
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

    /**
     * Checks the Worldguard version if Worldguard is enabled
     * @return the version as a String
     */
    @Override
    @SuppressWarnings("deprecation")
    public String getVersion() {
        if (WORLD_GUARD_PLUGIN == null) {
            return "undefined";
        } else {
            return WORLD_GUARD_PLUGIN.getDescription().getVersion().substring(0, 1);
        }
    }

    /**
     * This method tries to register a redstone-clock flag. If the user have a different plugin providing a flag
     * with the same name, an exception is thrown with a matching message
     * @return boolean flagloaded = true if the flag was able to load, if not return false
     */
    @Override
    public boolean registerFlag() {
        boolean flagLoaded = false;
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(REDSTONECLOCK_FLAG);
            flagLoaded = true;
        } catch (FlagConflictException e) {
            Bukkit.getLogger().severe("A plugin already uses the flag redstone-clock. WorldGuard flag support will not work");
        }
        return flagLoaded;
    }
}
