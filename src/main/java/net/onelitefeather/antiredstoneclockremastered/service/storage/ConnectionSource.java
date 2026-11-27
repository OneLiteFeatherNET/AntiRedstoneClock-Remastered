package net.onelitefeather.antiredstoneclockremastered.service.storage;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Opens a new JDBC connection to the database that holds the plugin state.
 *
 * <p>
 *     Callers close the returned connection themselves. No pooling is done, the plugin only runs a
 *     handful of small queries per server start and per admin join.
 * </p>
 */
@FunctionalInterface
public interface ConnectionSource {

    /**
     * Opens a connection.
     *
     * @return a new connection, the caller must close it
     * @throws SQLException if the database cannot be reached or the credentials are wrong
     */
    Connection open() throws SQLException;
}
