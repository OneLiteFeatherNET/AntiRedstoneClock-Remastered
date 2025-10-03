package net.onelitefeather.antiredstoneclockremastered.service.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.NotificationService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConsoleNotificationService implements NotificationService {

    private final AntiRedstoneClockRemastered plugin;
    private final NotificationService notificationService;

    public ConsoleNotificationService(@NotNull AntiRedstoneClockRemastered plugin,
                                      @Nullable NotificationService notificationService) {
        this.plugin = plugin;
        this.notificationService = notificationService;
    }

    @Override
    public Component getNotificationMessage(@NotNull Location location) {
        return MiniMessage.miniMessage().deserialize("Redstone Clock detected at: X,Y,Z(<x>,<y>,<z>)",
                Placeholder.component("x", Component.text(location.x())),
                Placeholder.component("y", Component.text(location.y())),
                Placeholder.component("z", Component.text(location.z()))
        );
    }

    @Override
    public void sendNotificationMessage(@NotNull Location location) {
        if (this.notificationService != null) {
            this.notificationService.sendNotificationMessage(location);
        }
        if (!isEnabled()) return;
        Bukkit.getConsoleSender().sendMessage(getNotificationMessage(location));
    }

    @Override
    public boolean isEnabled() {
        return this.plugin.getConfig().getBoolean("clock.notifyAdmins", true)
                || this.plugin.getConfig().getStringList("notification.enabled").contains("admins");
    }
}
