package net.onelitefeather.antiredstoneclockremastered.listener;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public final class ObserverListener implements Listener {
    private final Material material;
    private final AntiRedstoneClockRemastered plugin;
    private final CheckTPS checkTPS;
    private final RedstoneClockService redstoneClockService;

    @Inject
    public ObserverListener(AntiRedstoneClockRemastered plugin, CheckTPS checkTPS, RedstoneClockService redstoneClockService) {
        this.material = Material.OBSERVER;
        this.plugin = plugin;
        this.checkTPS = checkTPS;
        this.redstoneClockService = redstoneClockService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onRedstoneObserverClock(BlockRedstoneEvent blockRedstoneEvent) {

        if (!this.checkTPS.isTpsOk()) return;
        if (!this.plugin.getConfig().getBoolean("check.observer")) return;

        var block = blockRedstoneEvent.getBlock();
        var type = block.getType();
        if (type != material) return;

        if (blockRedstoneEvent.getOldCurrent() != 0) return;

        this.redstoneClockService.checkAndUpdateClockStateWithActive(block);
    }
}
