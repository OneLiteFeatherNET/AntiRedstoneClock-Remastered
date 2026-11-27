package net.onelitefeather.antiredstoneclockremastered.service.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the JDBC store against a real SQLite file. Every test gets its own temp directory.
 */
class JdbcDonationNoticeStoreTest {

    private static final UUID PLAYER = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final Instant FIRST = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant SECOND = Instant.parse("2026-02-01T12:30:00Z");

    @TempDir
    Path folder;

    private JdbcDonationNoticeStore storeFor(Path file) {
        return new JdbcDonationNoticeStore(new SqliteConnectionSource(file));
    }

    @Test
    @DisplayName("The first access creates both tables")
    void firstAccessCreatesTables() throws SQLException {
        var file = folder.resolve("storage.db");
        storeFor(file).lastShownAt(PLAYER);

        assertThat(tableNames(file))
                .as("tables after the first access")
                .contains("arcr_donation_notice", "arcr_state");
    }

    @Test
    @DisplayName("A player who was never reminded has no last shown instant")
    void unknownPlayerHasNoLastShownInstant() throws SQLException {
        var store = storeFor(folder.resolve("storage.db"));

        assertThat(store.lastShownAt(PLAYER)).as("last shown for an unknown player").isEmpty();
    }

    @Test
    @DisplayName("A written reminder instant is read back for the same player")
    void lastShownInstantRoundTrips() throws SQLException {
        var store = storeFor(folder.resolve("storage.db"));

        store.markShown(PLAYER, FIRST);

        assertThat(store.lastShownAt(PLAYER)).as("last shown after one write").contains(FIRST);
    }

    @Test
    @DisplayName("A second write replaces the earlier reminder instant")
    void secondWriteReplacesFirst() throws SQLException {
        var store = storeFor(folder.resolve("storage.db"));
        store.markShown(PLAYER, FIRST);

        store.markShown(PLAYER, SECOND);

        assertThat(store.lastShownAt(PLAYER)).as("last shown after two writes").contains(SECOND);
    }

    @Test
    @DisplayName("No console version is stored before the first console notice")
    void consoleVersionIsEmptyInitially() throws SQLException {
        var store = storeFor(folder.resolve("storage.db"));

        assertThat(store.consoleVersion()).as("console version on a fresh store").isEmpty();
    }

    @Test
    @DisplayName("A written console version is read back")
    void consoleVersionRoundTrips() throws SQLException {
        var store = storeFor(folder.resolve("storage.db"));

        store.setConsoleVersion("2.10.1");

        assertThat(store.consoleVersion()).as("console version after a write").contains("2.10.1");
    }

    @Test
    @DisplayName("Stored state is still there after the store is opened again on the same file")
    void stateSurvivesReopening() throws SQLException {
        var file = folder.resolve("storage.db");
        var first = storeFor(file);
        first.markShown(PLAYER, FIRST);
        first.setConsoleVersion("2.10.1");

        var reopened = storeFor(file);

        assertThat(reopened.lastShownAt(PLAYER)).as("reminder after reopening").contains(FIRST);
        assertThat(reopened.consoleVersion()).as("console version after reopening").contains("2.10.1");
    }

    private static java.util.List<String> tableNames(Path file) throws SQLException {
        var names = new java.util.ArrayList<String>();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath());
             ResultSet resultSet = connection.createStatement().executeQuery("SELECT name FROM sqlite_master WHERE type = 'table'")) {
            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }
        }
        return names;
    }
}
