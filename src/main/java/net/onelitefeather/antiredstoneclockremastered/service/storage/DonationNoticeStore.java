package net.onelitefeather.antiredstoneclockremastered.service.storage;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Remembers when donation reminders were shown and which plugin version was last announced in the console.
 */
public interface DonationNoticeStore {

    /**
     * @param playerId the player's UUID
     * @return when the reminder was last shown to this player, empty if never
     * @throws SQLException if the state cannot be read
     */
    Optional<Instant> lastShownAt(UUID playerId) throws SQLException;

    /**
     * Stores when the reminder was shown to this player, replacing any earlier value.
     *
     * @param playerId the player's UUID
     * @param shownAt  the time the reminder was shown
     * @throws SQLException if the state cannot be written
     */
    void markShown(UUID playerId, Instant shownAt) throws SQLException;

    /**
     * @return the plugin version the console notice was last shown for, empty if never
     * @throws SQLException if the state cannot be read
     */
    Optional<String> consoleVersion() throws SQLException;

    /**
     * @param version the plugin version the console notice was shown for
     * @throws SQLException if the state cannot be written
     */
    void setConsoleVersion(String version) throws SQLException;
}
