package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * The PistonListener is for all events that needs to be considered
 * when having a redstone piston block in a redstone clock / as a redstone clock for this plugin
 */
public final class PistonListener implements Listener {
    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    /**
     * A setter method for the PistonListener
     * @param antiRedstoneClockRemastered is another main class
     */
    public PistonListener(AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPistonExtendEvent(BlockPistonExtendEvent blockPistonExtendEvent) {

        if (!this.antiRedstoneClockRemastered.getTps().isTpsOk()) return;

        var block = blockPistonExtendEvent.getBlock();

        this.antiRedstoneClockRemastered.getRedstoneClockService().checkAndUpdateClockStateWithActiveManual(block, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPistonRetractEvent(BlockPistonRetractEvent blockPistonExtendEvent) {
        if (!this.antiRedstoneClockRemastered.getTps().isTpsOk()) return;

        var block = blockPistonExtendEvent.getBlock();

        this.antiRedstoneClockRemastered.getRedstoneClockService().checkAndUpdateClockStateWithActiveManual(block, true);
    }
}
