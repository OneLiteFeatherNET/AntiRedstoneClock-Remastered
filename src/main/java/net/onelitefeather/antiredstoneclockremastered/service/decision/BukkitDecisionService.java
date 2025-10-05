package net.onelitefeather.antiredstoneclockremastered.service.decision;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import net.onelitefeather.antiredstoneclockremastered.service.api.NotificationService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit/Paper implementation of the RedstoneClockService.
 * This implementation uses the standard Bukkit scheduler and APIs.
 *
 * @author TheMeinerLP
 * @version 2.2.0
 * @since 1.0.0
 */
public final class BukkitDecisionService implements DecisionService {

    private final @NotNull AntiRedstoneClockRemastered antiRedstoneClockRemastered;
    private final RegionService regionService;
    private final NotificationService notificationService;
    private final RedstoneClockMiddleware redstoneClockMiddleware;

    private final ItemStack SILK_TOUCH_PICKAXE = new ItemStack(Material.DIAMOND_PICKAXE);

    @Inject
    public BukkitDecisionService(@NotNull AntiRedstoneClockRemastered antiRedstoneClockRemastered,
                                 RedstoneClockMiddleware redstoneClockMiddleware,
                                 RegionService regionService, NotificationService notificationService) {
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
        this.redstoneClockMiddleware = redstoneClockMiddleware;
        this.regionService = regionService;
        this.notificationService = notificationService;
        SILK_TOUCH_PICKAXE.addEnchantment(Enchantment.SILK_TOUCH, 1);
    }

    @Override
    public void makeDecisionWithContext(RedstoneClockMiddleware.@NotNull CheckContext context) {
        var resultState = this.redstoneClockMiddleware.check(context);
        if (resultState == RedstoneClockMiddleware.ResultState.SKIP) {
            return;
        }
        this.notificationService.sendNotificationMessage(context.location());
        if (resultState == RedstoneClockMiddleware.ResultState.ONLY_NOTIFY) {
            return;
        }

        var location = context.location();

        if (resultState == RedstoneClockMiddleware.ResultState.REMOVE_AND_DROP) {
            Block block = location.getBlock();
            var drops = block.getDrops(SILK_TOUCH_PICKAXE);
            drops.forEach(itemStack -> block.getWorld().dropItem(location, itemStack));
            Runnable removeTask = () -> block.setType(Material.AIR, true);
            if (this.regionService.isRegionOwner(location)) {
                this.regionService.executeInRegion(location, removeTask);
            }
            return;
        }
        if (resultState == RedstoneClockMiddleware.ResultState.REMOVE_AND_WITHOUT_DROP) {
            Block block = location.getBlock();
            Runnable removeTask = () -> block.setType(Material.AIR, true);
            if (this.regionService.isRegionOwner(location)) {
                this.regionService.executeInRegion(location, removeTask);
            }
        }
    }

    @Override
    public void reload() {
        this.antiRedstoneClockRemastered.reloadConfig();
    }
}