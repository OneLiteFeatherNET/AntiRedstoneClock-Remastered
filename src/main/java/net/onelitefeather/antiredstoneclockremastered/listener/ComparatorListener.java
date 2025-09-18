package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.Plugin;

/**
 * Listener for handling comparator-based redstone clocks.
 * Uses dependency injection for better testability and maintainability.
 *
 * @author TheMeinerLP
 * @version 2.2.0
 * @since 1.0.0
 */
public final class ComparatorListener implements Listener {
    private final Material comparatorMaterial;
    private final RedstoneClockService redstoneClockService;
    private final CheckTPS checkTPS;
    private final FileConfiguration config;

    public ComparatorListener(Material comparatorMaterial, RedstoneClockService redstoneClockService, 
                            CheckTPS checkTPS, Plugin plugin) {
        this.comparatorMaterial = comparatorMaterial;
        this.redstoneClockService = redstoneClockService;
        this.checkTPS = checkTPS;
        this.config = plugin.getConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onRedstoneComparatorClock(BlockRedstoneEvent blockRedstoneEvent) {

        if (!this.checkTPS.isTpsOk()) return;
        if (!this.config.getBoolean("check.comparator")) return;

        var block = blockRedstoneEvent.getBlock();
        var type = block.getType();
        if (type != comparatorMaterial) return;

        if (blockRedstoneEvent.getOldCurrent() != 0) return;

        this.redstoneClockService.checkAndUpdateClockState(block);
    }
}
