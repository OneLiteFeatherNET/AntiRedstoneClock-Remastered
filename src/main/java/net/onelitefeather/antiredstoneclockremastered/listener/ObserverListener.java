package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * The ObserverListener is for all events that needs to be considered
 * when having a redstone observer block in a redstone clock / as a redstone clock for this plugin
 */
public final class ObserverListener implements Listener {
    private final Material material;
    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    /**
     * A setter method for the ObserverListener
     * @param antiRedstoneClockRemastered is another main class
     */
    public ObserverListener(AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.material = Material.OBSERVER;
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onRedstoneObserverClock(BlockRedstoneEvent blockRedstoneEvent) {

        if (!this.antiRedstoneClockRemastered.getTps().isTpsOk()) return;

        var block = blockRedstoneEvent.getBlock();
        var type = block.getType();
        if (type != material) return;

        if (blockRedstoneEvent.getOldCurrent() != 0) return;

        this.antiRedstoneClockRemastered.getRedstoneClockService().checkAndUpdateClockStateWithActive(block);
    }
}
