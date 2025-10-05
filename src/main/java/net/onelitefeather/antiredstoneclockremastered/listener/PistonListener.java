package net.onelitefeather.antiredstoneclockremastered.listener;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public final class PistonListener implements Listener {
    private final DecisionService decisionService;

    @Inject
    public PistonListener(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPistonExtendEvent(BlockPistonExtendEvent blockPistonExtendEvent) {
        var block = blockPistonExtendEvent.getBlock();
        this.decisionService.makeDecisionWithContext(RedstoneClockMiddleware.CheckContext.of(block, false, RedstoneClockMiddleware.EventType.PISTON));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPistonRetractEvent(BlockPistonRetractEvent blockPistonExtendEvent) {
        var block = blockPistonExtendEvent.getBlock();
        this.decisionService.makeDecisionWithContext(RedstoneClockMiddleware.CheckContext.of(block, true, RedstoneClockMiddleware.EventType.PISTON));
    }
}
