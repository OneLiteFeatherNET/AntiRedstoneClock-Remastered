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
     * Some checks to ensure if the redstone clock is in Plotsquared / Worldguard allowed with custom flags with allowed worlds
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

    /**
     * Using the above method checkAndUpdateClockStateWithActiveManual but using the block where the redstone clock is located
     * @param block The block where the redstone clock is located
     * @param state If the clock is enabled or disabled
     */
    public void checkAndUpdateClockStateWithActiveManual(@NotNull Block block, boolean state) {
        checkAndUpdateClockStateWithActiveManual(block.getLocation(), state);
    }

    /**
     * Add the block to get the location of the redstone clock location
     * @param block the block of the clock
     */
    public void checkAndUpdateClockStateWithActive(@NotNull Block block) {
        checkAndUpdateClockStateWithActive(block.getLocation());
    }

    /**
     * Some checks for allowed world and if the clock logic is allowed in Plotsquared and Worldguard with the custom flag
     * Also some destruction logic to remove the clock if it is not allowed
     * @param location the location of the redstone clock
     */
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

    /**
     * Uses the location of the block to call the method below
     * @param block the block of the clock
     */
    public void checkAndUpdateClockState(@NotNull Block block) {
        checkAndUpdateClockState(block.getLocation());
    }

    /**
     * Same checks as with the manual method
     * @param location the location of the redstone clock
     */
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

    /**
     * The actual destruction method where the not allowed and tested redstone clock gets destroyed with a message
     * @param location the location of the clock
     * @param clock the detected and not allowed clock
     */
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

    /**
     * Checks if the config was set to dropitems or not. If true, only natural blocks can drop, if set false, nothing will drop
     * @param location the location of the redstone clock which is destroyed
     */
    private void breakBlock(@NotNull Location location) {
        if (this.dropItems) {
            location.getBlock().breakNaturally();
        } else {
            location.getBlock().setType(Material.AIR, false);
        }
    }

    /**
     * Adds a Redstone Clock to the hashmap with a location and an endtime where it can time out and then removed again
     * @param location the location of the redstone clock
     */
    public void addRedstoneClockTest(@NotNull Location location) {
        this.activeClockTesters.putIfAbsent(location, new RedstoneClock(location, (System.currentTimeMillis() / 1000) + endTimeDelay));
    }

    /**
     * Removes the redstone clock location out of the hashmap
     * @param location of the redstone clock
     */
    public void removeClockByLocation(@NotNull Location location) {
        this.activeClockTesters.remove(location);
    }

    /**
     * Uses the above method but with the matching location of the redstone clock
     * @param redstoneClock the redstone clock object
     */
    public void removeClockByClock(@NotNull RedstoneClock redstoneClock) {
        removeClockByLocation(redstoneClock.getLocation());
    }

    /**
     *
     * @param location of the Redstone Clock
     * @return boolean if the hashmap contains the key with the redstone location
     */
    public boolean containsLocation(@NotNull Location location) {
        return this.activeClockTesters.containsKey(location);
    }

    /**
     * Getter method for the key location of the hashmap
     * @param location of the Redstone Clock
     * @return RedstoneClock
     */
    @Nullable
    public RedstoneClock getClockByLocation(@NotNull Location location) {
        return this.activeClockTesters.get(location);
    }

    /**
     * A getter method to get all active Redstone Clocks
     * @return Collection - a list of Redstone Clocks that are active as the value of the hashmap
     */
    @NotNull
    public Collection<RedstoneClock> getRedstoneClocks() {
        return Collections.unmodifiableCollection(this.activeClockTesters.values());
    }

    /**
     * A getter method to get all Redstone Clock locations
     * @return Collection - a list of Redstone Clock locations as a key of the hashmap
     */
    @NotNull
    public Collection<Location> getRedstoneClockLocations() {
        return Collections.unmodifiableCollection(this.activeClockTesters.keySet());
    }

    /**
     * A getter method to get all active clock testers that test redstone clocks
     * @return the copy of the hashmap as a map
     */
    @NotNull
    public Map<Location, RedstoneClock> getActiveTester() {
        return Map.copyOf(this.activeClockTesters);
    }

}
