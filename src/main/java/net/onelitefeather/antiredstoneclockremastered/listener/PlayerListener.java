package net.onelitefeather.antiredstoneclockremastered.listener;

import net.kyori.adventure.text.Component;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerListener implements Listener {

    private final RedstoneClockService redstoneClockService;

    public PlayerListener(RedstoneClockService redstoneClockService) {
        this.redstoneClockService = redstoneClockService;
    }

    @EventHandler
    private void onPlayerJoinEvent(PlayerJoinEvent playerJoinEvent) {
        var player = playerJoinEvent.getPlayer();
        if (player.isOp() || player.hasPermission(Constants.PERMISSION_NOTIFY)) {
            player.sendMessage(Component.translatable("antiredstoneclockremastered.notify.donation.player").arguments(AntiRedstoneClockRemastered.PREFIX));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        var block = blockBreakEvent.getBlock();
        recheck:
        if (Constants.REDSTONE_ITEMS.contains(block.getType())) {
            this.redstoneClockService.removeClockByLocation(block.getLocation());
        } else {
            block = block.getRelative(BlockFace.UP);
            break recheck;
        }
    }
}
