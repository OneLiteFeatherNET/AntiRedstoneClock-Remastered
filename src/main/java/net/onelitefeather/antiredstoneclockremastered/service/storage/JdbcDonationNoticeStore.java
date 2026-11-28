package net.onelitefeather.antiredstoneclockremastered.service.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link DonationNoticeStore} over plain JDBC.
 *
 * <p>
 *     Only SQL that is valid in both SQLite and MariaDB is used, so one implementation serves both.
 *     The tables are created on the first access of each instance.
 * </p>
 */
public final class JdbcDonationNoticeStore implements DonationNoticeStore {

    static final String CONSOLE_VERSION_KEY = "donation-console-version";

    private static final String CREATE_NOTICE_TABLE = """
            CREATE TABLE IF NOT EXISTS arcr_donation_notice (
                player_uuid CHAR(36) NOT NULL PRIMARY KEY,
                last_shown_at BIGINT NOT NULL
            )""";
    private static final String CREATE_STATE_TABLE = """
            CREATE TABLE IF NOT EXISTS arcr_state (
                state_key VARCHAR(64) NOT NULL PRIMARY KEY,
                state_value VARCHAR(255) NOT NULL
            )""";
    private static final String SELECT_LAST_SHOWN = "SELECT last_shown_at FROM arcr_donation_notice WHERE player_uuid = ?";
    private static final String REPLACE_LAST_SHOWN = "REPLACE INTO arcr_donation_notice (player_uuid, last_shown_at) VALUES (?, ?)";
    private static final String SELECT_STATE = "SELECT state_value FROM arcr_state WHERE state_key = ?";
    private static final String REPLACE_STATE = "REPLACE INTO arcr_state (state_key, state_value) VALUES (?, ?)";

    private final ConnectionSource connectionSource;
    private volatile boolean schemaReady;

    public JdbcDonationNoticeStore(ConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }

    @Override
    public Optional<Instant> lastShownAt(UUID playerId) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(SELECT_LAST_SHOWN)) {
            statement.setString(1, playerId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(Instant.ofEpochMilli(resultSet.getLong(1))) : Optional.empty();
            }
        }
    }

    @Override
    public void markShown(UUID playerId, Instant shownAt) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(REPLACE_LAST_SHOWN)) {
            statement.setString(1, playerId.toString());
            statement.setLong(2, shownAt.toEpochMilli());
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<String> consoleVersion() throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(SELECT_STATE)) {
            statement.setString(1, CONSOLE_VERSION_KEY);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.ofNullable(resultSet.getString(1)) : Optional.empty();
            }
        }
    }

    @Override
    public void setConsoleVersion(String version) throws SQLException {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(REPLACE_STATE)) {
            statement.setString(1, CONSOLE_VERSION_KEY);
            statement.setString(2, version);
            statement.executeUpdate();
        }
    }

    /**
     * Opens a connection and makes sure both tables exist.
     *
     * <p>
     *     If the schema cannot be created the connection is closed again and the error is passed on,
     *     so the next call retries.
     * </p>
     */
    private Connection open() throws SQLException {
        var connection = connectionSource.open();
        if (schemaReady) {
            return connection;
        }
        try (var statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_NOTICE_TABLE);
            statement.executeUpdate(CREATE_STATE_TABLE);
            schemaReady = true;
            return connection;
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }
}
