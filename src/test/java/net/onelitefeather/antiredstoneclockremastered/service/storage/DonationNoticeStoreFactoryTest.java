package net.onelitefeather.antiredstoneclockremastered.service.storage;

import net.onelitefeather.antiredstoneclockremastered.service.DonationNoticeService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * A broken MariaDB setting must never stop the plugin from loading. These tests use ports and hosts that fail
 * at once, so no test waits on the network.
 */
class DonationNoticeStoreFactoryTest {

    @TempDir
    Path folder;

    private static YamlConfiguration mariaDbConfig(String host, int port) {
        var config = new YamlConfiguration();
        config.set("storage.type", "mariadb");
        config.set("storage.mariadb.host", host);
        config.set("storage.mariadb.port", port);
        config.set("storage.mariadb.database", "antiredstoneclock");
        config.set("storage.mariadb.username", "user");
        config.set("storage.mariadb.password", "secret");
        return config;
    }

    private DonationNoticeService serviceFor(YamlConfiguration config) {
        var store = DonationNoticeStoreFactory.create(config, folder);
        return new DonationNoticeService(store, Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC), true, 30, "2.10.1");
    }

    @Test
    @DisplayName("Creating a MariaDB store for an unreachable server does not throw")
    void unreachableMariaDbServerDoesNotThrowOnCreation() {
        assertThatCode(() -> DonationNoticeStoreFactory.create(mariaDbConfig("127.0.0.1", 1), folder))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("An unreachable MariaDB server makes the service skip the reminder")
    void unreachableMariaDbServerMeansNoReminder() {
        var service = serviceFor(mariaDbConfig("127.0.0.1", 1));

        assertThatCode(() -> assertThat(service.shouldRemind(UUID.randomUUID())).as("reminder with an unreachable server").isFalse())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Creating a MariaDB store with a nonsensical port does not throw")
    void nonsensicalMariaDbPortDoesNotThrowOnCreation() {
        assertThatCode(() -> DonationNoticeStoreFactory.create(mariaDbConfig("127.0.0.1", -1), folder))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A nonsensical MariaDB port makes the service skip the reminder")
    void nonsensicalMariaDbPortMeansNoReminder() {
        var service = serviceFor(mariaDbConfig("127.0.0.1", -1));

        assertThatCode(() -> assertThat(service.shouldRemind(UUID.randomUUID())).as("reminder with a nonsensical port").isFalse())
                .doesNotThrowAnyException();
    }
}
