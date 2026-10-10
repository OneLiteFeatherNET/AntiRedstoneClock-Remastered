package net.onelitefeather.antiredstoneclockremastered.listener;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockReceiveGameEvent;

import java.util.Set;

/**
 * Counts vibrations received by sculk sensors.
 * <p>
 * The server fires {@link BlockReceiveGameEvent} for every vibration listener (sensors, shriekers, and the
 * position of listening mobs) and fires it already cancelled when the sensor cannot receive the vibration
 * at the moment, for example during its active or cooldown phase. Only the events a sensor really accepts
 * and that a block caused (piston, note block, ...) can be part of a clock. Vibrations caused by players
 * or mobs are not self sustaining.
 */
public final class SculkListener implements Listener {

    // Resolved by name: the calibrated sculk sensor does not exist before 1.20.
    private static final Set<String> SENSOR_TYPES = Set.of("SCULK_SENSOR", "CALIBRATED_SCULK_SENSOR");

    private final DecisionService decisionService;

    @Inject
    public SculkListener(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onBlockReceiveGameEvent(BlockReceiveGameEvent event) {
        if (event.getEntity() != null) return;
        var block = event.getBlock();
        if (!SENSOR_TYPES.contains(block.getType().name())) return;
        this.decisionService.makeDecisionWithContext(
                RedstoneClockMiddleware.CheckContext.of(block, true, RedstoneClockMiddleware.EventType.SCULK_SENSOR));
    }

}
