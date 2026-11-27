package net.onelitefeather.antiredstoneclockremastered.service.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Builds the {@link DonationNoticeStore} that matches the {@code storage} section of the config.
 */
public final class DonationNoticeStoreFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonationNoticeStoreFactory.class);

    private DonationNoticeStoreFactory() {
        // Utility class
    }

    /**
     * Creates the store. No connection is opened here, the database is only contacted on first use.
     *
     * @param config     the plugin configuration
     * @param dataFolder the plugin folder, the SQLite file is placed inside it
     * @return the store for the configured database type
     */
    public static DonationNoticeStore create(FileConfiguration config, Path dataFolder) {
        var type = config.getString("storage.type", "sqlite");
        ConnectionSource source;
        if ("mariadb".equalsIgnoreCase(type)) {
            source = new MariaDbConnectionSource(
                    config.getString("storage.mariadb.host", "localhost"),
                    config.getInt("storage.mariadb.port", 3306),
                    config.getString("storage.mariadb.database", "antiredstoneclock"),
                    config.getString("storage.mariadb.username", ""),
                    config.getString("storage.mariadb.password", "")
            );
        } else {
            if (!"sqlite".equalsIgnoreCase(type)) {
                LOGGER.warn("Unknown storage.type '{}', falling back to sqlite", type);
            }
            source = new SqliteConnectionSource(dataFolder.resolve(config.getString("storage.sqlite.file", "storage.db")));
        }
        return new JdbcDonationNoticeStore(source);
    }
}
