package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * The ComparatorListener is for all events that needs to be considered
 * when having a redstone comparator block in a redstone clock / as a redstone clock for this plugin
 */
public final class ComparatorListener implements Listener {
    private final Material comparatorMaterial;
    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    /**
     * A setter method for the ComparatorListener
     * @param comparatorMaterial is the bukkit Material of the Comparator
     * @param antiRedstoneClockRemastered is another main class
     */
    public ComparatorListener(Material comparatorMaterial, AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.comparatorMaterial = comparatorMaterial;
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onRedstoneComparatorClock(BlockRedstoneEvent blockRedstoneEvent) {

        if (!this.antiRedstoneClockRemastered.getTps().isTpsOk()) return;

        var block = blockRedstoneEvent.getBlock();
        var type = block.getType();
        if (type != comparatorMaterial) return;

        if (blockRedstoneEvent.getOldCurrent() != 0) return;

        this.antiRedstoneClockRemastered.getRedstoneClockService().checkAndUpdateClockState(block);
    }
}
