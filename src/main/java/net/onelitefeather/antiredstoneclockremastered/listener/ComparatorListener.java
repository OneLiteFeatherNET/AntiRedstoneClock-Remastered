package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public final class ComparatorListener implements Listener {
    private final Material comparatorMaterial;
    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    public ComparatorListener(Material comparatorMaterial, AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.comparatorMaterial = comparatorMaterial;
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onRedstoneComparatorClock(BlockRedstoneEvent blockRedstoneEvent) {

        if (!this.antiRedstoneClockRemastered.getTps().isTpsOk()) return;
        if (!this.antiRedstoneClockRemastered.getConfig().getBoolean("check.comparator")) return;

        var block = blockRedstoneEvent.getBlock();
        var type = block.getType();
        if (type != comparatorMaterial) return;

        if (blockRedstoneEvent.getOldCurrent() != 0) return;

        this.antiRedstoneClockRemastered.getRedstoneClockService().checkAndUpdateClockState(block);
    }
}
