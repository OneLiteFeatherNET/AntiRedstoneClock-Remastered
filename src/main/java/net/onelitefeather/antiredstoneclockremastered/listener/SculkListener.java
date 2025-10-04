package net.onelitefeather.antiredstoneclockremastered.listener;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockReceiveGameEvent;

public final class SculkListener implements Listener {

    private final DecisionService decisionService;

    @Inject
    public SculkListener(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockReceiveGameEvent(BlockReceiveGameEvent event) {
        var block = event.getBlock();
        this.decisionService.makeDecisionWithContext(
                RedstoneClockMiddleware.CheckContext.of(block, RedstoneClockMiddleware.EventType.SCULK_SENSOR));
    }

}
