package net.onelitefeather.antiredstoneclockremastered.service.impl;

import jakarta.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Bukkit/Paper implementation of the RedstoneClockService.
 * This implementation uses the standard Bukkit scheduler and APIs.
 *
 * @author TheMeinerLP
 * @version 2.2.0
 * @since 1.0.0
 */
public final class BukkitRedstoneClockService implements RedstoneClockService {

    private final @NotNull AntiRedstoneClockRemastered antiRedstoneClockRemastered;
    private final RegionService regionService;
    private int endTimeDelay;
    private int maxClockCount;
    private boolean autoBreakBlock;
    private boolean notifyAdmins;
    private boolean notifyConsole;
    private boolean dropItems;
    private List<String> ignoredWorlds;

    private final ConcurrentHashMap<Location, RedstoneClock> activeClockTesters = new ConcurrentHashMap<>();
    private final ItemStack SILK_TOUCH_PICKAXE = new ItemStack(Material.DIAMOND_PICKAXE);

    @Inject
    public BukkitRedstoneClockService(@NotNull AntiRedstoneClockRemastered antiRedstoneClockRemastered, RegionService regionService) {
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
        this.endTimeDelay = antiRedstoneClockRemastered.getConfig().getInt("clock.endDelay", 300);
        this.maxClockCount = antiRedstoneClockRemastered.getConfig().getInt("clock.maxCount", 150);
        this.autoBreakBlock = antiRedstoneClockRemastered.getConfig().getBoolean("clock.autoBreak", true);
        this.notifyAdmins = antiRedstoneClockRemastered.getConfig().getBoolean("clock.notifyAdmins", true);
        this.notifyConsole = antiRedstoneClockRemastered.getConfig().getBoolean("clock.notifyConsole", true);
        this.dropItems = antiRedstoneClockRemastered.getConfig().getBoolean("clock.drop", false);
        this.ignoredWorlds = antiRedstoneClockRemastered.getConfig().getStringList("check.ignoredWorlds");
        this.regionService = regionService;
        SILK_TOUCH_PICKAXE.addEnchantment(Enchantment.SILK_TOUCH, 1);
    }

    @Override
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

    @Override
    public void checkAndUpdateClockStateWithActiveManual(@NotNull Block block, boolean state) {
        checkAndUpdateClockStateWithActiveManual(block.getLocation(), state);
    }

    @Override
    public void checkAndUpdateClockStateWithActive(@NotNull Block block) {
        checkAndUpdateClockStateWithActive(block.getLocation());
    }

    @Override
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

    @Override
    public void checkAndUpdateClockState(@NotNull Block block) {
        checkAndUpdateClockState(block.getLocation());
    }

    @Override
    public void checkAndUpdateClockState(@NotNull Location location) {
        if (this.ignoredWorlds.contains(location.getWorld().getName())) return;
        if (this.antiRedstoneClockRemastered.getWorldGuardSupport() != null && this.antiRedstoneClockRemastered.getWorldGuardSupport().isRegionAllowed(location))
            return;
        if (this.antiRedstoneClockRemastered.getPlotsquaredSupport() != null && this.antiRedstoneClockRemastered.getPlotsquaredSupport().isAllowedPlot(location))
            return;
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
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isOp() || player.hasPermission(Constants.PERMISSION_NOTIFY)) {
                        sendNotification(player, location);
                    }
                }
            }

        }
        removeClockByClock(clock);
    }

    private void sendNotification(final Player player, final Location location) {
        final var component = Component.translatable("service.notify.detected.clock")
                .arguments(AntiRedstoneClockRemastered.PREFIX,
                        Component.text(location.getBlockX()),
                        Component.text(location.getBlockY()),
                        Component.text(location.getBlockZ()),
                        Component.empty().clickEvent(ClickEvent.callback(audience -> {
                            if (audience instanceof final Player executor) {
                                executor.teleport(location);
                            }
                        })));
        player.sendMessage(component);
    }

    private void breakBlock(@NotNull Location location) {
        Block block = location.getBlock();
        if (this.dropItems) {
            var drops = block.getDrops(SILK_TOUCH_PICKAXE);
            drops.forEach(itemStack -> block.getWorld().dropItem(location, itemStack));
        }
        Runnable removeTask = () -> block.setType(Material.AIR, true);
        if (this.regionService.isRegionOwner(location)) {
            this.regionService.executeInRegion(location, removeTask);
        }

    }

    @Override
    public void addRedstoneClockTest(@NotNull Location location) {
        this.activeClockTesters.putIfAbsent(location, new RedstoneClock(location, (System.currentTimeMillis() / 1000) + endTimeDelay));
    }

    @Override
    public void reload() {
        this.antiRedstoneClockRemastered.reloadConfig();
        this.endTimeDelay = antiRedstoneClockRemastered.getConfig().getInt("clock.endDelay", 300);
        this.maxClockCount = antiRedstoneClockRemastered.getConfig().getInt("clock.maxCount", 150);
        this.autoBreakBlock = antiRedstoneClockRemastered.getConfig().getBoolean("clock.autoBreak", true);
        this.notifyAdmins = antiRedstoneClockRemastered.getConfig().getBoolean("clock.notifyAdmins", true);
        this.notifyConsole = antiRedstoneClockRemastered.getConfig().getBoolean("clock.notifyConsole", true);
        this.dropItems = antiRedstoneClockRemastered.getConfig().getBoolean("clock.drop", false);
        this.ignoredWorlds = antiRedstoneClockRemastered.getConfig().getStringList("check.ignoredWorlds");
    }

    @Override
    public void removeClockByLocation(@NotNull Location location) {
        this.activeClockTesters.remove(location);
    }

    @Override
    public void removeClockByClock(@NotNull RedstoneClock redstoneClock) {
        removeClockByLocation(redstoneClock.getLocation());
    }

    @Override
    public boolean containsLocation(@NotNull Location location) {
        return this.activeClockTesters.containsKey(location);
    }

    @Override
    @Nullable
    public RedstoneClock getClockByLocation(@NotNull Location location) {
        return this.activeClockTesters.get(location);
    }

    @Override
    @NotNull
    public Collection<RedstoneClock> getRedstoneClocks() {
        return Collections.unmodifiableCollection(this.activeClockTesters.values());
    }

    @Override
    @NotNull
    public Collection<Location> getRedstoneClockLocations() {
        return Collections.unmodifiableCollection(this.activeClockTesters.keySet());
    }

    @Override
    @NotNull
    public Map<Location, RedstoneClock> getActiveTester() {
        return Map.copyOf(this.activeClockTesters);
    }
}