package net.onelitefeather.antiredstoneclockremastered.listener;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.kyori.adventure.text.Component;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerListener implements Listener {

    private final RedstoneTrackingService trackingService;
    private final UpdateService updateService;

    @Inject
    public PlayerListener(RedstoneTrackingService trackingService, UpdateService updateService) {
        this.trackingService = trackingService;
        this.updateService = updateService;
    }

    @EventHandler
    private void onPlayerJoinEvent(PlayerJoinEvent playerJoinEvent) {
        var player = playerJoinEvent.getPlayer();
        if (player.isOp() || (player.hasPermission(Constants.PERMISSION_NOTIFY) && !player.hasPermission(Constants.DISABLE_DONATION_NOTIFY))) {
            player.sendMessage(Component.translatable("antiredstoneclockremastered.notify.donation.player").arguments(AntiRedstoneClockRemastered.PREFIX));
        }
        if (player.isOp() || player.hasPermission(Constants.PERMISSION_NOTIFY_UPDATE)) {
            this.updateService.notifyPlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        var block = blockBreakEvent.getBlock();
        recheck:
        if (Constants.REDSTONE_ITEMS.contains(block.getType())) {
            this.trackingService.removeClockByLocation(block.getLocation());
        } else {
            block = block.getRelative(BlockFace.UP);
            break recheck;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSignBlockBreak(BlockBreakEvent blockBreakEvent) {
        var block = blockBreakEvent.getBlock();
        var hasKey = block.hasMetadata(Constants.META_KEY_ARCR_SIGN);
        if (hasKey) {
            blockBreakEvent.setDropItems(false);
        }
    }
}
