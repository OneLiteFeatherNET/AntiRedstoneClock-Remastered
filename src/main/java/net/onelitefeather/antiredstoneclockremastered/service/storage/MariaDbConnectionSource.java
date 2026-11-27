package net.onelitefeather.antiredstoneclockremastered.service.storage;

import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Opens connections to a MariaDB server.
 *
 * <p>
 *     The driver is shaded and relocated, so it is instantiated directly instead of being looked up
 *     through {@code DriverManager}, whose service discovery does not see relocated drivers.
 * </p>
 *
 * <p>
 *     Nothing is done in the constructor, so a bad setting can never stop the plugin from loading. Every
 *     problem, including an unparsable URL, is reported as an {@link SQLException} from {@link #open()}.
 * </p>
 */
public final class MariaDbConnectionSource implements ConnectionSource {

    private final String url;
    private final String username;
    private final String password;

    public MariaDbConnectionSource(String host, int port, String database, String username, String password) {
        this.url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection open() throws SQLException {
        try {
            var dataSource = new MariaDbDataSource(url);
            dataSource.setUser(username);
            dataSource.setPassword(password);
            return dataSource.getConnection();
        } catch (RuntimeException e) {
            throw new SQLException("Invalid MariaDB settings: " + e.getMessage(), e);
        }
    }
}
