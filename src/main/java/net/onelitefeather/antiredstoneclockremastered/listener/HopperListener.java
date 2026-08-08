package net.onelitefeather.antiredstoneclockremastered.listener;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.tracking.HopperClockTracker;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

/**
 * Listener for handling hopper clocks.
 *
 * <p>Two hoppers pointing at each other move the same item back and forth, which keeps the
 * server busy without any redstone being involved. Only such a change of direction is
 * reported, so hopper chains of sorting systems are not counted as clocks.</p>
 *
 * @author OneLiteFeather
 * @version 1.0.0
 * @since 2.9.0
 */
public final class HopperListener implements Listener {

    private final AntiRedstoneClockRemastered plugin;
    private final DecisionService decisionService;
    private final HopperClockTracker tracker;

    @Inject
    public HopperListener(AntiRedstoneClockRemastered plugin, DecisionService decisionService) {
        this.plugin = plugin;
        this.decisionService = decisionService;
        this.tracker = new HopperClockTracker();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onInventoryMoveItem(InventoryMoveItemEvent inventoryMoveItemEvent) {
        // This event fires for every single item movement on the server, so bail out as early as possible.
        // The same check runs in SkipEventTypeRedstoneClockMiddleware, this one only saves the chain call.
        if (!this.plugin.getConfig().getBoolean("check.hopper")) return;

        var sourceLocation = inventoryMoveItemEvent.getSource().getLocation();
        if (sourceLocation == null || sourceLocation.getBlock().getType() != Material.HOPPER) return;

        var destinationLocation = inventoryMoveItemEvent.getDestination().getLocation();
        if (destinationLocation == null || destinationLocation.getBlock().getType() != Material.HOPPER) return;

        var sourceKey = HopperClockTracker.keyOf(sourceLocation);
        var destinationKey = HopperClockTracker.keyOf(destinationLocation);
        if (sourceKey == null || destinationKey == null) return;

        var timeout = this.plugin.getConfig().getInt("clock.endDelay", 300);
        if (!this.tracker.registerMovement(sourceKey, destinationKey, System.currentTimeMillis() / 1000, timeout)) {
            return;
        }

        // Always report the same hopper of the pair, otherwise every cycle would be tracked as its own clock.
        var pair = HopperClockTracker.HopperPair.of(sourceKey, destinationKey);
        var clockBlock = pair.first().equals(sourceKey)
                ? sourceLocation.getBlock()
                : destinationLocation.getBlock();

        this.decisionService.makeDecisionWithContext(
                RedstoneClockMiddleware.CheckContext.of(clockBlock, true, RedstoneClockMiddleware.EventType.HOPPER));
    }
}
