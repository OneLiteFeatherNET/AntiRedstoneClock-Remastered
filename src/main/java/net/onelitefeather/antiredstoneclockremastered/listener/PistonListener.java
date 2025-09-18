package net.onelitefeather.antiredstoneclockremastered.listener;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

public final class PistonListener implements Listener {
    private final AntiRedstoneClockRemastered plugin;
    private final CheckTPS checkTPS;
    private final RedstoneClockService redstoneClockService;

    @Inject
    public PistonListener(AntiRedstoneClockRemastered plugin, CheckTPS checkTPS, RedstoneClockService redstoneClockService) {
        this.plugin = plugin;
        this.checkTPS = checkTPS;
        this.redstoneClockService = redstoneClockService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPistonExtendEvent(BlockPistonExtendEvent blockPistonExtendEvent) {

        if (!this.checkTPS.isTpsOk()) return;
        if (!this.plugin.getConfig().getBoolean("check.piston")) return;

        var block = blockPistonExtendEvent.getBlock();

        this.redstoneClockService.checkAndUpdateClockStateWithActiveManual(block, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPistonRetractEvent(BlockPistonRetractEvent blockPistonExtendEvent) {
        if (!this.checkTPS.isTpsOk()) return;
        if (!this.plugin.getConfig().getBoolean("check.piston")) return;

        var block = blockPistonExtendEvent.getBlock();

        this.redstoneClockService.checkAndUpdateClockStateWithActiveManual(block, true);
    }
}
