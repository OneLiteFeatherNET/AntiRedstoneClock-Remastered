package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockReceiveGameEvent;

public final class SculkListener implements Listener {

    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    public SculkListener(AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockReceiveGameEvent(BlockReceiveGameEvent event) {
        if (!this.antiRedstoneClockRemastered.getTps().isTpsOk()) return;
        var block = event.getBlock();
        this.antiRedstoneClockRemastered.getRedstoneClockService().checkAndUpdateClockState(block);
    }

}
