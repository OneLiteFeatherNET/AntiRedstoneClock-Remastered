package net.onelitefeather.antiredstoneclockremastered.listener;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockReceiveGameEvent;

public final class SculkListener implements Listener {

    private final AntiRedstoneClockRemastered plugin;
    private final CheckTPS checkTPS;
    private final RedstoneClockService redstoneClockService;

    @Inject
    public SculkListener(AntiRedstoneClockRemastered plugin, CheckTPS checkTPS, RedstoneClockService redstoneClockService) {
        this.plugin = plugin;
        this.checkTPS = checkTPS;
        this.redstoneClockService = redstoneClockService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onBlockReceiveGameEvent(BlockReceiveGameEvent event) {
        if (!this.checkTPS.isTpsOk()) return;
        if (!this.plugin.getConfig().getBoolean("check.sculk")) return;
        var block = event.getBlock();
        this.redstoneClockService.checkAndUpdateClockState(block);
    }

}
