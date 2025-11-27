package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * Listener for handling comparator-based redstone clocks.
 * Uses dependency injection for better testability and maintainability.
 *
 * @author TheMeinerLP
 * @version 2.2.0
 * @since 1.0.0
 */
public final class ComparatorListener implements Listener {
    private final Material comparatorMaterial;
    private final DecisionService decisionService;

    public ComparatorListener(Material comparatorMaterial, DecisionService decisionService) {
        this.comparatorMaterial = comparatorMaterial;
        this.decisionService = decisionService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onRedstoneComparatorClock(BlockRedstoneEvent blockRedstoneEvent) {
        var block = blockRedstoneEvent.getBlock();
        var type = block.getType();
        if (type != comparatorMaterial) return;

        if (blockRedstoneEvent.getOldCurrent() != 0) return;

        this.decisionService.makeDecisionWithContext(RedstoneClockMiddleware.CheckContext.of(block, RedstoneClockMiddleware.EventType.COMPARATOR));
    }
}
