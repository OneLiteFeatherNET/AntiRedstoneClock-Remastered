package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public final class RedstoneListener implements Listener {
    private final Material reperaterMaterial;
    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    public RedstoneListener(Material reperaterMaterial, AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.reperaterMaterial = reperaterMaterial;
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onRedstoneClock(BlockRedstoneEvent blockRedstoneEvent) {

        if (!this.antiRedstoneClockRemastered.getTps().isTpsOk()) return;
        if (!this.antiRedstoneClockRemastered.getConfig().getBoolean("check.redstoneAndRepeater")) return;

        var block = blockRedstoneEvent.getBlock();
        var type = block.getType();
        if (type != Material.REDSTONE_WIRE && type != reperaterMaterial) return;

        if (blockRedstoneEvent.getOldCurrent() != 0) return;

        this.antiRedstoneClockRemastered.getRedstoneClockService().checkAndUpdateClockState(block);
    }
}
