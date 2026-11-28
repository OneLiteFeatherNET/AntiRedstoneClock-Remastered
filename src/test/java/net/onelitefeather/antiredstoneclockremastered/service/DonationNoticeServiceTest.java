package net.onelitefeather.antiredstoneclockremastered.service;

import net.onelitefeather.antiredstoneclockremastered.service.storage.InMemoryDonationNoticeStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DonationNoticeServiceTest {

    private static final UUID ADMIN = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final int INTERVAL_DAYS = 30;

    /** A clock the test moves forward explicitly, no system time is read. */
    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            this.now = this.now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final InMemoryDonationNoticeStore store = new InMemoryDonationNoticeStore();
    private final MutableClock clock = new MutableClock(START);

    private DonationNoticeService service(boolean enabled, String version) {
        return new DonationNoticeService(store, clock, enabled, INTERVAL_DAYS, version);
    }

    @Test
    @DisplayName("An admin who was never reminded is reminded")
    void firstJoinReminds() {
        assertThat(service(true, "2.10.1").shouldRemind(ADMIN)).as("first join").isTrue();
    }

    @Test
    @DisplayName("An admin reminded a moment ago is not reminded again")
    void joinInsideIntervalDoesNotRemind() {
        var service = service(true, "2.10.1");
        service.markReminded(ADMIN);
        clock.advance(Duration.ofDays(INTERVAL_DAYS - 1));

        assertThat(service.shouldRemind(ADMIN)).as("join one day before the interval ends").isFalse();
    }

    @Test
    @DisplayName("An admin is reminded again once the interval has passed")
    void joinAfterIntervalReminds() {
        var service = service(true, "2.10.1");
        service.markReminded(ADMIN);
        clock.advance(Duration.ofDays(INTERVAL_DAYS));

        assertThat(service.shouldRemind(ADMIN)).as("join exactly one interval later").isTrue();
    }

    @Test
    @DisplayName("No join reminder is shown while the notice is disabled")
    void disabledNoticeNeverReminds() {
        assertThat(service(false, "2.10.1").shouldRemind(ADMIN)).as("disabled notice").isFalse();
    }

    @Test
    @DisplayName("The console notice is shown on the first start of a version")
    void consoleNoticeShownForNewVersion() {
        assertThat(service(true, "2.10.1").claimConsoleNotice()).as("first start").isTrue();
    }

    @Test
    @DisplayName("The console notice is not shown again on a later start of the same version")
    void consoleNoticeNotShownTwiceForSameVersion() {
        var service = service(true, "2.10.1");
        service.claimConsoleNotice();

        assertThat(service.claimConsoleNotice()).as("second start of the same version").isFalse();
    }

    @Test
    @DisplayName("The console notice is shown again after the plugin was updated")
    void consoleNoticeShownAgainAfterVersionChange() {
        service(true, "2.10.1").claimConsoleNotice();

        assertThat(service(true, "2.10.2").claimConsoleNotice()).as("first start of the new version").isTrue();
    }

    @Test
    @DisplayName("The console notice is never shown while the notice is disabled")
    void disabledNoticeNeverShowsConsoleLine() {
        assertThat(service(false, "2.10.1").claimConsoleNotice()).as("disabled console notice").isFalse();
    }

    @Test
    @DisplayName("A failing store disables the join reminder without throwing")
    void failingStoreSkipsJoinReminder() {
        store.failing(true);
        var service = service(true, "2.10.1");

        assertThatCode(() -> assertThat(service.shouldRemind(ADMIN)).as("reminder with a failing store").isFalse())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A failing store does not throw when a reminder is marked")
    void failingStoreDoesNotThrowOnMarkReminded() {
        store.failing(true);
        var service = service(true, "2.10.1");

        assertThatCode(() -> service.markReminded(ADMIN)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A failing store skips the console notice without throwing")
    void failingStoreSkipsConsoleNotice() {
        store.failing(true);
        var service = service(true, "2.10.1");

        assertThatCode(() -> assertThat(service.claimConsoleNotice()).as("console notice with a failing store").isFalse())
                .doesNotThrowAnyException();
    }
}
