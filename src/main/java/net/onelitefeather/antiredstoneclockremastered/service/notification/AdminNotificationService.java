package net.onelitefeather.antiredstoneclockremastered.service.notification;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.NotificationService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AdminNotificationService implements NotificationService {

    private final AntiRedstoneClockRemastered plugin;
    private final NotificationService notificationService;

    public AdminNotificationService(@NotNull AntiRedstoneClockRemastered plugin,
                                      @Nullable NotificationService notificationService) {
        this.plugin = plugin;
        this.notificationService = notificationService;
    }

    @Override
    public Component getNotificationMessage(@NotNull Location location) {
        return Component.translatable("service.notify.detected.clock")
                .arguments(AntiRedstoneClockRemastered.PREFIX,
                        Component.text(location.getBlockX()),
                        Component.text(location.getBlockY()),
                        Component.text(location.getBlockZ()),
                        Component.empty().clickEvent(ClickEvent.callback(audience -> {
                            if (audience instanceof final Player executor) {
                                executor.teleport(location);
                            }
                        })));
    }

    @Override
    public void sendNotificationMessage(@NotNull Location location) {
        if (this.notificationService != null) {
            this.notificationService.sendNotificationMessage(location);
        }
        if (!isEnabled()) return;
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(Constants.PERMISSION_NOTIFY) || !player.isOp()) continue;
            player.sendMessage(getNotificationMessage(location));
        }
    }

    @Override
    public boolean isEnabled() {
        return  this.plugin.getConfig().getBoolean("clock.notifyAdmins", true)
                || this.plugin.getConfig().getStringList("notification.enabled").contains("admins");
    }
}
