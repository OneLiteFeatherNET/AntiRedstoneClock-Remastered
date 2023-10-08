package net.onelitefeather.antiredstoneclockremastered.service;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
import java.util.logging.Level;

public final class RedstoneClockService {

    private final static TagResolver.Single PREFIX = Placeholder.component("prefix", MiniMessage.miniMessage().deserialize("<gradient:red:white>[AntiRedstoneClock]</gradient>"));

    private final @NotNull AntiRedstoneClockRemastered antiRedstoneClockRemastered;
    private final int endTimeDelay;
    private final int maxClockCount;
    private final boolean autoBreakBlock;
    private final boolean notifyAdmins;
    private final boolean notifyConsole;
    private final boolean dropItems;
    private final List<String> ignoredWorlds;

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

    public void checkAndUpdateClockStateWithActiveManual(@NotNull Location location, boolean state) {
        if (this.ignoredWorlds.contains(location.getWorld().getName())) return;
        if (this.antiRedstoneClockRemastered.getWorldGuardSupport() != null && this.antiRedstoneClockRemastered.getWorldGuardSupport().isRegionAllowed(location))
            return;
        if (this.antiRedstoneClockRemastered.getPlotsquaredSupport() != null && this.antiRedstoneClockRemastered.getPlotsquaredSupport().isAllowedPlot(location))
            return;
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
        if (this.antiRedstoneClockRemastered.getWorldGuardSupport() != null && this.antiRedstoneClockRemastered.getWorldGuardSupport().isRegionAllowed(location))
            return;
        if (this.antiRedstoneClockRemastered.getPlotsquaredSupport() != null && this.antiRedstoneClockRemastered.getPlotsquaredSupport().isAllowedPlot(location))
            return;
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
        if (this.antiRedstoneClockRemastered.getWorldGuardSupport() != null &&
                this.antiRedstoneClockRemastered.getWorldGuardSupport().isRegionAllowed(location)) return;
        if (this.antiRedstoneClockRemastered.getPlotsquaredSupport() != null &&
                this.antiRedstoneClockRemastered.getPlotsquaredSupport().isAllowedPlot(location)) return;
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
                this.antiRedstoneClockRemastered.getLogger().log(Level.WARNING, "Redstone Clock detected at: X,Y,Z({0},{1},{2})", new Object[]{location.getBlockX(), location.getBlockY(), location.getBlockZ()});
            }
            if (this.notifyAdmins) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isOp() || player.hasPermission(Constants.PERMISSION_NOTIFY)) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<prefix> <gold><click:run_command:'/tp <x> <y> <z>'>Redstone Clock detected at: X,Y,Z(<x>,<y>,<z>)</click>",
                                Placeholder.parsed("x", String.valueOf(location.getBlockX())),
                                Placeholder.parsed("y", String.valueOf(location.getBlockY())),
                                Placeholder.parsed("z", String.valueOf(location.getBlockZ())),
                                PREFIX));
                    }
                }
            }

        }
        removeClockByClock(clock);
    }

    private void breakBlock(@NotNull Location location) {
        Block block = location.getBlock();
        if (this.dropItems) {
            var drops = block.getDrops();
            drops.forEach(itemStack -> block.getWorld().dropItem(location, itemStack));
        }
        Runnable removeTask = () -> block.setType(Material.AIR, true);
        Bukkit.getScheduler().runTaskLater(antiRedstoneClockRemastered, removeTask, 1);

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
