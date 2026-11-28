package net.onelitefeather.antiredstoneclockremastered.service.storage;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens connections to a SQLite database file.
 *
 * <p>
 *     The sqlite-jdbc driver is provided by the Paper server, it is not shaded into the plugin.
 * </p>
 */
public final class SqliteConnectionSource implements ConnectionSource {

    private static final int BUSY_TIMEOUT_MILLIS = 3_000;

    private final String url;

    public SqliteConnectionSource(Path file) {
        this.url = "jdbc:sqlite:" + file.toAbsolutePath();
    }

    @Override
    public Connection open() throws SQLException {
        var properties = new Properties();
        properties.setProperty("busy_timeout", String.valueOf(BUSY_TIMEOUT_MILLIS));
        return DriverManager.getConnection(url, properties);
    }
}
