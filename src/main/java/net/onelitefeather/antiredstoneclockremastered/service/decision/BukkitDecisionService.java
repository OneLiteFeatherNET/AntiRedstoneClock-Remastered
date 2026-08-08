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
        LOGGER.debug("Handling {} clock at {}", context.eventType(), context.location());
        this.notificationService.sendNotificationMessage(context.location());
        if (resultState == RedstoneClockMiddleware.ResultState.ONLY_NOTIFY) {
            return;
        }

        var location = context.location();

        if (resultState == RedstoneClockMiddleware.ResultState.REMOVE_AND_DROP) {
            Block block = location.getBlock();
            var drops = block.getDrops(SILK_TOUCH_PICKAXE);
            drops.forEach(itemStack -> block.getWorld().dropItem(location, itemStack));
            removeBlock(block, location);
            return;
        }
        if (resultState == RedstoneClockMiddleware.ResultState.REMOVE_AND_WITHOUT_DROP) {
            removeBlock(location.getBlock(), location);
        }
    }

    private void removeBlock(@NotNull Block block, @NotNull Location location) {
        if (!this.regionService.isRegionOwner(location)) {
            LOGGER.warn("Could not remove the redstone clock at {}, its region is not owned by the current thread", location);
            return;
        }
        LOGGER.debug("Removing {} at {}", block.getType(), location);
        this.regionService.executeInRegion(location, () -> block.setType(Material.AIR, true));
    }

    @Override
    public void reload() {
        this.antiRedstoneClockRemastered.reloadPluginConfig();
    }
}