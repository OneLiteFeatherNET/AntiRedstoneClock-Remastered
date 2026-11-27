package net.onelitefeather.antiredstoneclockremastered.listener;

import net.kyori.adventure.text.Component;
import net.onelitefeather.antiredstoneclockremastered.service.DonationNoticeService;
import net.onelitefeather.antiredstoneclockremastered.service.api.SchedulerService;
import net.onelitefeather.antiredstoneclockremastered.service.storage.InMemoryDonationNoticeStore;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the join notice that {@link DonationNoticeListener} sends to admins.
 */
class DonationNoticeListenerTest {

    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-00000000abcd");

    private InMemoryDonationNoticeStore store;
    private DonationNoticeListener listener;

    @BeforeEach
    void setUp() {
        this.store = new InMemoryDonationNoticeStore();
        var donationNoticeService = new DonationNoticeService(store, Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC), true, 30, "2.10.1");
        // Runs every task inline, the real implementations hand it to the async or global scheduler.
        this.listener = new DonationNoticeListener(donationNoticeService, new InlineSchedulerService());
    }

    private static Player player(UUID id, boolean op, boolean notifyPermission, boolean disablePermission) {
        var player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(id);
        when(player.isOp()).thenReturn(op);
        when(player.hasPermission(Constants.PERMISSION_NOTIFY)).thenReturn(notifyPermission);
        when(player.hasPermission(Constants.DISABLE_DONATION_NOTIFY)).thenReturn(disablePermission);
        return player;
    }

    private void join(Player player) throws Exception {
        var event = mock(PlayerJoinEvent.class);
        when(event.getPlayer()).thenReturn(player);
        var method = DonationNoticeListener.class.getDeclaredMethod("onPlayerJoinEvent", PlayerJoinEvent.class);
        method.setAccessible(true);
        method.invoke(this.listener, event);
    }

    @Test
    @DisplayName("A player without admin rights never touches the notice store")
    void nonAdminNeverAccessesStore() throws Exception {
        join(player(PLAYER_ID, false, false, false));

        assertThat(store.accesses()).as("store accesses for a non-admin").isZero();
    }

    @Test
    @DisplayName("An admin with the donation notice disabled by permission never touches the notice store")
    void permissionOptOutNeverAccessesStore() throws Exception {
        join(player(PLAYER_ID, false, true, true));

        assertThat(store.accesses()).as("store accesses with the disable permission").isZero();
    }

    @Test
    @DisplayName("An admin who is due receives the donation message")
    void dueAdminReceivesMessage() throws Exception {
        var admin = player(PLAYER_ID, true, false, false);

        join(admin);

        verify(admin).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("An admin who was reminded within the interval receives no donation message")
    void adminInsideIntervalReceivesNoMessage() throws Exception {
        join(player(PLAYER_ID, true, false, false));
        var second = player(PLAYER_ID, true, false, false);

        join(second);

        verify(second, never()).sendMessage(any(Component.class));
    }

    /** Runs every scheduled task on the calling thread. */
    private static final class InlineSchedulerService implements SchedulerService {
        @Override
        public void scheduleTask(Runnable task) {
            task.run();
        }

        @Override
        public void scheduleAsyncTask(Runnable task) {
            task.run();
        }

        @Override
        public void scheduleRepeatingTask(Runnable task, long delay, long period) {
            throw new UnsupportedOperationException();
        }

        @Override
        public io.papermc.paper.threadedregions.scheduler.ScheduledTask runTaskTimerAsynchronously(Consumer<io.papermc.paper.threadedregions.scheduler.ScheduledTask> task, long delay, long period) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cancelTasks() {
            // nothing scheduled
        }
    }
}
