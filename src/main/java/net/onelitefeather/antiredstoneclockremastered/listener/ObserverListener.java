package net.onelitefeather.antiredstoneclockremastered.listener;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public final class ObserverListener implements Listener {
    private final Material material;
    private final DecisionService decisionService;

    @Inject
    public ObserverListener(DecisionService decisionService) {
        this.material = Material.OBSERVER;
        this.decisionService = decisionService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onRedstoneObserverClock(BlockRedstoneEvent blockRedstoneEvent) {

        var block = blockRedstoneEvent.getBlock();
        var type = block.getType();
        if (type != material) return;
        if (blockRedstoneEvent.getOldCurrent() != 0) return;


        this.decisionService.makeDecisionWithContext(
                RedstoneClockMiddleware.CheckContext.of(block, true, RedstoneClockMiddleware.EventType.OBSERVER));
    }
}
