package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * Listener for handling redstone repeater-based redstone clocks.
 * Uses dependency injection for better testability and maintainability.
 *
 * @author TheMeinerLP
 * @version 2.2.0
 * @since 1.0.0
 */
public final class RedstoneListener implements Listener {
    private final Material repeaterMaterial;
    private final DecisionService decisionService;

    public RedstoneListener(Material repeaterMaterial, DecisionService decisionService) {
        this.repeaterMaterial = repeaterMaterial;
        this.decisionService = decisionService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onRedstoneClock(BlockRedstoneEvent blockRedstoneEvent) {
        var block = blockRedstoneEvent.getBlock();
        var type = block.getType();
        if (type != Material.REDSTONE_WIRE && type != repeaterMaterial) return;
        if (blockRedstoneEvent.getOldCurrent() != 0) return;
        this.decisionService.makeDecisionWithContext(
                RedstoneClockMiddleware.CheckContext.of(block, RedstoneClockMiddleware.EventType.REDSTONE_AND_REPEATER));
    }
}
