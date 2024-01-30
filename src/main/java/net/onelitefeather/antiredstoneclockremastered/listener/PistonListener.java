package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

public final class PistonListener implements Listener {
    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    public PistonListener(AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPistonExtendEvent(BlockPistonExtendEvent blockPistonExtendEvent) {

        if (!this.antiRedstoneClockRemastered.getTps().isTpsOk()) return;
        if (!this.antiRedstoneClockRemastered.getConfig().getBoolean("check.piston")) return;

        var block = blockPistonExtendEvent.getBlock();

        this.antiRedstoneClockRemastered.getRedstoneClockService().checkAndUpdateClockStateWithActiveManual(block, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPistonRetractEvent(BlockPistonRetractEvent blockPistonExtendEvent) {
        if (!this.antiRedstoneClockRemastered.getTps().isTpsOk()) return;
        if (!this.antiRedstoneClockRemastered.getConfig().getBoolean("check.piston")) return;

        var block = blockPistonExtendEvent.getBlock();

        this.antiRedstoneClockRemastered.getRedstoneClockService().checkAndUpdateClockStateWithActiveManual(block, true);
    }
}
