package net.onelitefeather.antiredstoneclockremastered.service;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The RedstoneClockService is not only getting the options from the config, it executes detection and redstone clock destruction logic
 * if the flags in the config are set to true and or no worlds are ignored in the config
 * If Plotsquared / Worldguard is not enabled on the server, it will be ignored as well
 */
public final class RedstoneClockService {

    private final @NotNull AntiRedstoneClockRemastered antiRedstoneClockRemastered;
    private final int endTimeDelay;
    /**
     * config value (integer for example on default 300 in seconds) for redstone timeout (maxClockCount reset)
     */
    private final int maxClockCount;
    /**
     * config value (true or false) if the block should break if a redstone clock has been detected and stopped
     */
    private final boolean autoBreakBlock;
    /**
     * Config value (true or false) if the users with the matching permission antiredstoneclockremastered.notify.admin
     * are notified
     */
    private final boolean notifyAdmins;
    /**
     * That is a value in the config that can be set to
     * true / false if the console should log if a redstone clock was detected or not
     */
    private final boolean notifyConsole;
    /**
     * Config value (true or false) if the plugin drops the item for the player if the redstone clock was detected and destroyed
     */
    private final boolean dropItems;
    /**
     * ignoredWorlds is a list of worlds that won't be considered for clock detection
     */
    private final List<String> ignoredWorlds;

    /**
     * This is a list of Redstone Clocks with their matching location
     */
    private final ConcurrentHashMap<Location, RedstoneClock> activeClockTesters = new ConcurrentHashMap<>();

    public RedstoneClockService(@NotNull AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
        this.endTimeDelay = antiRedstoneClockRemastered.getConfig().getInt("clock.endDelay", 300);
        this.maxClockCount = antiRedstoneClockRemastered.getConfig().getInt("clock.maxCount", 150);
        this.autoBreakBlock = antiRedstoneClockRemastered.getConfig().getBoolean("clock.autoBreak", true);
        this.notifyAdmins = antiRedstoneClockRemastered.getConfig().getBoolean("clock.notifyAdmins", true);
        this.notifyConsole = antiRedstoneClockRemastered.getConfig().getBoolean("clock.notifyConsole", true);
        this.dropItems = antiRedstoneClockRemastered.getConfig().getBoolean("clock.drop", false);
        this.ignoredWorlds = antiRedstoneClockRemastered.getConfig().getStringList("check.ignoredWorlds");
    }

    /**
     * Some checks to ensure if the redstone clock is in dependency regions with enabled custom flags with allowed worlds
     * @param location the location of the redstone clock
     * @param state can be whether enabled or not, if not, it can run into a timeout when it was enabled before
     */
    public void checkAndUpdateClockStateWithActiveManual(@NotNull Location location, boolean state) {
        if (this.ignoredWorlds.contains(location.getWorld().getName())) return;
        if (this.antiRedstoneClockRemastered.getWorldGuardSupport().isRegionAllowed(location)) return;
        if (this.antiRedstoneClockRemastered.getPlotsquaredSupport().isAllowedPlot(location)) return;
        var clock = getClockByLocation(location);
        if (clock != null) {
            if (clock.isActive()) {
                if (clock.isTimeOut()) {
                    removeClockByClock(clock);
                    return;
                }
                if (clock.getTriggerCount() >= this.maxClockCount) {
                    destroyRedstoneClock(location, clock);
                    return;
                }
                clock.incrementTriggerCount();
                clock.setActive(false);
                return;
            } else {
                clock.setActive(state);
            }
        }
        addRedstoneClockTest(location);
    }

    public void checkAndUpdateClockStateWithActiveManual(@NotNull Block block, boolean state) {
        checkAndUpdateClockStateWithActiveManual(block.getLocation(), state);
    }
    public void checkAndUpdateClockStateWithActive(@NotNull Block block) {
        checkAndUpdateClockStateWithActive(block.getLocation());
    }

    public void checkAndUpdateClockStateWithActive(@NotNull Location location) {
        if (this.ignoredWorlds.contains(location.getWorld().getName())) return;
        if (this.antiRedstoneClockRemastered.getWorldGuardSupport().isRegionAllowed(location)) return;
        if (this.antiRedstoneClockRemastered.getPlotsquaredSupport().isAllowedPlot(location)) return;
        var clock = getClockByLocation(location);
        if (clock != null) {
            if (clock.isActive()) {
                if (clock.isTimeOut()) {
                    removeClockByClock(clock);
                    return;
                }
                if (clock.getTriggerCount() >= this.maxClockCount) {
                    destroyRedstoneClock(location, clock);
                    return;
                }
                clock.incrementTriggerCount();
                clock.setActive(false);
                return;
            } else {
                clock.setActive(true);
            }
        }
        addRedstoneClockTest(location);
    }

    public void checkAndUpdateClockState(@NotNull Block block) {
        checkAndUpdateClockState(block.getLocation());
    }

    public void checkAndUpdateClockState(@NotNull Location location) {
        if (this.ignoredWorlds.contains(location.getWorld().getName())) return;
        if (this.antiRedstoneClockRemastered.getWorldGuardSupport().isRegionAllowed(location)) return;
        if (this.antiRedstoneClockRemastered.getPlotsquaredSupport().isAllowedPlot(location)) return;
        var clock = getClockByLocation(location);
        if (clock != null) {
            if (clock.isTimeOut()) {
                removeClockByClock(clock);
                return;
            }
            if (clock.getTriggerCount() >= this.maxClockCount) {
                destroyRedstoneClock(location, clock);
                return;
            }
            clock.incrementTriggerCount();
            return;
        }
        addRedstoneClockTest(location);
    }

    private void destroyRedstoneClock(@NotNull Location location, @NotNull RedstoneClock clock) {
        if (this.autoBreakBlock) breakBlock(location);
        if (!clock.isDetected()) {
            clock.setDetected(true);
            if (this.notifyConsole) {
                this.antiRedstoneClockRemastered.getLogger().warning("Redstone Clock detected at: X,Y,Z(DEMO,DEMO,DEMO)");
            }
            if (this.notifyAdmins) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isOp() || player.hasPermission(Constants.PERMISSION_NOTIFY)) {
                        player.sendMessage(""); //TODO: Message
                    }
                }
            }

        }
        removeClockByClock(clock);
    }

    private void breakBlock(@NotNull Location location) {
        if (this.dropItems) {
            location.getBlock().breakNaturally();
        } else {
            location.getBlock().setType(Material.AIR, false);
        }
    }

    public void addRedstoneClockTest(@NotNull Location location) {
        this.activeClockTesters.putIfAbsent(location, new RedstoneClock(location, (System.currentTimeMillis() / 1000) + endTimeDelay));
    }

    public void removeClockByLocation(@NotNull Location location) {
        this.activeClockTesters.remove(location);
    }

    public void removeClockByClock(@NotNull RedstoneClock redstoneClock) {
        removeClockByLocation(redstoneClock.getLocation());
    }

    public boolean containsLocation(@NotNull Location location) {
        return this.activeClockTesters.containsKey(location);
    }

    @Nullable
    public RedstoneClock getClockByLocation(@NotNull Location location) {
        return this.activeClockTesters.get(location);
    }

    @NotNull
    public Collection<RedstoneClock> getRedstoneClocks() {
        return Collections.unmodifiableCollection(this.activeClockTesters.values());
    }

    @NotNull
    public Collection<Location> getRedstoneClockLocations() {
        return Collections.unmodifiableCollection(this.activeClockTesters.keySet());
    }

    @NotNull
    public Map<Location, RedstoneClock> getActiveTester() {
        return Map.copyOf(this.activeClockTesters);
    }

}
