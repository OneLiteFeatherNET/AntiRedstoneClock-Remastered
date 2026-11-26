package net.onelitefeather.antiredstoneclockremastered.service.decision;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import net.onelitefeather.antiredstoneclockremastered.service.api.NotificationService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(BukkitDecisionService.class);

    private final @NotNull AntiRedstoneClockRemastered antiRedstoneClockRemastered;
    private final RegionService regionService;
    private final NotificationService notificationService;
    private final RedstoneClockMiddleware redstoneClockMiddleware;

    private final ItemStack SILK_TOUCH_PICKAXE;

    @Inject
    public BukkitDecisionService(@NotNull AntiRedstoneClockRemastered antiRedstoneClockRemastered,
                                 RedstoneClockMiddleware redstoneClockMiddleware,
                                 RegionService regionService, NotificationService notificationService) {
        this(antiRedstoneClockRemastered, redstoneClockMiddleware, regionService, notificationService, silkTouchPickaxe());
    }

    /**
     * Creates the service with the tool whose drops are used, so it can be built without a running server.
     */
    BukkitDecisionService(@NotNull AntiRedstoneClockRemastered antiRedstoneClockRemastered,
                          RedstoneClockMiddleware redstoneClockMiddleware,
                          RegionService regionService, NotificationService notificationService,
                          @NotNull ItemStack silkTouchPickaxe) {
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
        this.redstoneClockMiddleware = redstoneClockMiddleware;
        this.regionService = regionService;
        this.notificationService = notificationService;
        this.SILK_TOUCH_PICKAXE = silkTouchPickaxe;
    }

    private static @NotNull ItemStack silkTouchPickaxe() {
        var pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        pickaxe.addEnchantment(Enchantment.SILK_TOUCH, 1);
        return pickaxe;
    }

    @Override
    public void makeDecisionWithContext(RedstoneClockMiddleware.@NotNull CheckContext context) {
        var resultState = this.redstoneClockMiddleware.check(context);
        if (resultState == RedstoneClockMiddleware.ResultState.SKIP) {
            return;
        }
        LOGGER.debug("Handling {} clock at {}", context.eventType(), context.location());
        this.notificationService.sendNotificationMessage(context.location());
        if (resultState == RedstoneClockMiddleware.ResultState.ONLY_NOTIFY) {
            return;
        }

        var location = context.location();

        if (resultState == RedstoneClockMiddleware.ResultState.REMOVE_AND_DROP) {
            removeBlock(location.getBlock(), location, true);
            return;
        }
        if (resultState == RedstoneClockMiddleware.ResultState.REMOVE_AND_WITHOUT_DROP) {
            removeBlock(location.getBlock(), location, false);
        }
    }

    private void removeBlock(@NotNull Block block, @NotNull Location location, boolean drop) {
        if (!this.regionService.isRegionOwner(location)) {
            LOGGER.warn("Could not remove the redstone clock at {}, its region is not owned by the current thread", location);
            return;
        }
        var detectedType = block.getType();
        LOGGER.debug("Removing {} at {}", detectedType, location);
        this.regionService.executeInRegion(location, () -> {
            // the block may have been moved or replaced since the detection, never touch what is there now
            if (block.getType() != detectedType) {
                LOGGER.debug("Not removing the redstone clock at {}, {} was replaced by {}", location, detectedType, block.getType());
                return;
            }
            if (drop) {
                if (block.getState() instanceof Container container) {
                    // only this block's own inventory, not the other half of a double chest; cleared before
                    // the drops are computed so a shulker box item does not carry the contents a second time
                    var inventory = container instanceof Chest chest ? chest.getBlockInventory() : container.getInventory();
                    for (var itemStack : inventory.getContents()) {
                        if (itemStack != null && !itemStack.isEmpty()) {
                            block.getWorld().dropItem(location, itemStack);
                        }
                    }
                    inventory.clear();
                }
                block.getDrops(SILK_TOUCH_PICKAXE).forEach(itemStack -> block.getWorld().dropItem(location, itemStack));
            }
            block.setType(Material.AIR, true);
        });
    }

    @Override
    public void reload() {
        this.antiRedstoneClockRemastered.reloadPluginConfig();
    }
}