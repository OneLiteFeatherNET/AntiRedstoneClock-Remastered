package net.onelitefeather.antiredstoneclockremastered.service.api;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * Service for sending notifications.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 2.4.0
 */
public interface NotificationService {

    /**
     * Get the notification message for the given location.
     * @param location where the notification gets sent from
     * @return the notification message
     */
    @Nullable Component getNotificationMessage(Location location);

    /**
     * Send a notification message to the given audience.
     * @param location where the notification gets sent from
     */
    void sendNotificationMessage(Location location);

    boolean isEnabled();
}
