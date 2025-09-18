package net.onelitefeather.antiredstoneclockremastered.listener;

import net.kyori.adventure.text.Component;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerListener implements Listener {

    private final RedstoneClockService redstoneClockService;
    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    public PlayerListener(RedstoneClockService redstoneClockService, AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.redstoneClockService = redstoneClockService;
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @EventHandler
    private void onPlayerJoinEvent(PlayerJoinEvent playerJoinEvent) {
        var player = playerJoinEvent.getPlayer();
        if (player.isOp() || (player.hasPermission(Constants.PERMISSION_NOTIFY) && !player.hasPermission(Constants.DISABLE_DONATION_NOTIFY))) {
            player.sendMessage(Component.translatable("antiredstoneclockremastered.notify.donation.player").arguments(AntiRedstoneClockRemastered.PREFIX));
        }
        if (player.isOp() || player.hasPermission(Constants.PERMISSION_NOTIFY_UPDATE)) {
            this.antiRedstoneClockRemastered.getUpdateService().notifyPlayer(player);
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
