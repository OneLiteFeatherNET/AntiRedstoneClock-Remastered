package net.onelitefeather.antiredstoneclockremastered.service;

import net.onelitefeather.antiredstoneclockremastered.service.storage.DonationNoticeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

/**
 * Decides when the donation notice is shown to admins on join and when it is logged to the console.
 *
 * <p>
 *     Methods that touch the database must be called off the server thread. A database failure never
 *     propagates: the notice is skipped and one warning is logged, so the plugin keeps working without a
 *     reachable database.
 * </p>
 */
public final class DonationNoticeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationNoticeService.class);

    private final DonationNoticeStore store;
    private final Clock clock;
    private final boolean enabled;
    private final Duration interval;
    private final String pluginVersion;

    /**
     * @param store          where the reminder state is kept
     * @param clock          the time source, injected so tests can control it
     * @param enabled        {@code donation.notice}, false turns off both the join and the console notice
     * @param intervalDays   {@code donation.intervalDays}, the days between two join reminders for one admin
     * @param pluginVersion  the running plugin version, the console notice is shown once per version
     */
    public DonationNoticeService(DonationNoticeStore store, Clock clock, boolean enabled, int intervalDays, String pluginVersion) {
        this.store = store;
        this.clock = clock;
        this.enabled = enabled;
        this.interval = Duration.ofDays(intervalDays);
        this.pluginVersion = pluginVersion;
    }

    /**
     * @param playerId the joining admin
     * @return true if the notice is enabled and the admin has never seen it or last saw it at least one interval ago
     */
    public boolean shouldRemind(UUID playerId) {
        if (!enabled) {
            return false;
        }
        try {
            return store.lastShownAt(playerId)
                    .map(lastShown -> !clock.instant().isBefore(lastShown.plus(interval)))
                    .orElse(true);
        } catch (SQLException e) {
            LOGGER.warn("Could not read the donation notice state, the notice is skipped: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Remembers that the notice was shown to the admin now.
     *
     * @param playerId the admin the notice was shown to
     */
    public void markReminded(UUID playerId) {
        try {
            store.markShown(playerId, clock.instant());
        } catch (SQLException e) {
            LOGGER.warn("Could not store the donation notice state: {}", e.getMessage());
        }
    }

    /**
     * Checks whether the console notice is due for the running version and, if so, stores the version.
     *
     * @return true if the console notice should be logged now
     */
    public boolean claimConsoleNotice() {
        if (!enabled) {
            return false;
        }
        try {
            if (store.consoleVersion().filter(pluginVersion::equals).isPresent()) {
                return false;
            }
            store.setConsoleVersion(pluginVersion);
            return true;
        } catch (SQLException e) {
            LOGGER.warn("Could not read or store the donation console state, the notice is skipped: {}", e.getMessage());
            return false;
        }
    }
}
