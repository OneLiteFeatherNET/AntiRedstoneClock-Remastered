package net.onelitefeather.antiredstoneclockremastered.listener;

import jakarta.inject.Inject;
import net.kyori.adventure.text.Component;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.DonationNoticeService;
import net.onelitefeather.antiredstoneclockremastered.service.api.SchedulerService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Shows the donation reminder to admins on join, at most once per interval.
 *
 * <p>
 *     The database lookup runs off the server thread, the message is sent back on the server thread.
 * </p>
 */
public final class DonationNoticeListener implements Listener {

    private final DonationNoticeService donationNoticeService;
    private final SchedulerService schedulerService;

    @Inject
    public DonationNoticeListener(DonationNoticeService donationNoticeService, SchedulerService schedulerService) {
        this.donationNoticeService = donationNoticeService;
        this.schedulerService = schedulerService;
    }

    @EventHandler
    private void onPlayerJoinEvent(PlayerJoinEvent playerJoinEvent) {
        var player = playerJoinEvent.getPlayer();
        if (!(player.isOp() || (player.hasPermission(Constants.PERMISSION_NOTIFY) && !player.hasPermission(Constants.DISABLE_DONATION_NOTIFY)))) {
            return;
        }
        var playerId = player.getUniqueId();
        this.schedulerService.scheduleAsyncTask(() -> {
            if (!this.donationNoticeService.shouldRemind(playerId)) {
                return;
            }
            this.donationNoticeService.markReminded(playerId);
            this.schedulerService.scheduleTask(() -> player.sendMessage(Component.translatable("antiredstoneclockremastered.notify.donation.player").arguments(AntiRedstoneClockRemastered.PREFIX)));
        });
    }
}
