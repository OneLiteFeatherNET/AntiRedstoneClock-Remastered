package net.onelitefeather.antiredstoneclockremastered.service.storage;

import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Test double for {@link DonationNoticeStore}. Keeps the state in memory and counts every access, so tests can
 * assert that the store was not touched. Set {@link #failing} to make every call throw.
 */
public final class InMemoryDonationNoticeStore implements DonationNoticeStore {

    private final Map<UUID, Instant> lastShown = new HashMap<>();
    private String consoleVersion;
    private int accesses;
    private boolean failing;

    public int accesses() {
        return accesses;
    }

    public void failing(boolean failing) {
        this.failing = failing;
    }

    private void access() throws SQLException {
        accesses++;
        if (failing) {
            throw new SQLException("database is down");
        }
    }

    @Override
    public Optional<Instant> lastShownAt(UUID playerId) throws SQLException {
        access();
        return Optional.ofNullable(lastShown.get(playerId));
    }

    @Override
    public void markShown(UUID playerId, Instant shownAt) throws SQLException {
        access();
        lastShown.put(playerId, shownAt);
    }

    @Override
    public Optional<String> consoleVersion() throws SQLException {
        access();
        return Optional.ofNullable(consoleVersion);
    }

    @Override
    public void setConsoleVersion(String version) throws SQLException {
        access();
        consoleVersion = version;
    }
}
