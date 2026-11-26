package net.onelitefeather.antiredstoneclockremastered.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that breaking a redstone component only resets its clock counter when the break really happens.
 * Events are fired through MockBukkit's plugin manager so the handler's priority and cancellation settings apply.
 * The block is a Mockito mock because MockBukkit's world mock cannot initialise against the current Paper API.
 */
class PlayerListenerTest {

    private ServerMock server;
    private RedstoneTrackingService trackingService;
    private Block repeater;

    @BeforeEach
    void setUp() {
        this.server = MockBukkit.mock();
        var plugin = MockBukkit.createMockPlugin();
        this.trackingService = mock(RedstoneTrackingService.class);
        var listener = new PlayerListener(this.trackingService, mock(UpdateService.class));
        this.server.getPluginManager().registerEvents(listener, plugin);
        this.repeater = mock(Block.class);
        when(this.repeater.getType()).thenReturn(Material.REPEATER);
        when(this.repeater.getLocation()).thenReturn(new Location(null, 0, 64, 0));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private BlockBreakEvent breakRepeater() {
        return new BlockBreakEvent(this.repeater, mock(Player.class));
    }

    @Test
    @DisplayName("A cancelled break of a redstone component keeps its clock counter")
    void cancelledBreakKeepsClockCounter() {
        var event = breakRepeater();
        event.setCancelled(true);

        this.server.getPluginManager().callEvent(event);

        verify(this.trackingService, never().description(
                "a break cancelled by a protection plugin must not reset the clock counter"))
                .removeClockByLocation(any());
    }

    @Test
    @DisplayName("A break of a redstone component that happens removes its clock counter")
    void successfulBreakRemovesClockCounter() {
        this.server.getPluginManager().callEvent(breakRepeater());

        verify(this.trackingService).removeClockByLocation(new Location(null, 0, 64, 0));
    }

    @Test
    @DisplayName("The block break handler only observes breaks that are not cancelled")
    void blockBreakHandlerMonitorsUncancelledBreaks() throws Exception {
        var handler = PlayerListener.class.getDeclaredMethod("onBlockBreak", BlockBreakEvent.class)
                .getAnnotation(EventHandler.class);

        assertThat(handler.ignoreCancelled()).as("cancelled breaks must be skipped").isTrue();
        assertThat(handler.priority())
                .as("the handler must run after protection plugins have decided")
                .isEqualTo(EventPriority.MONITOR);
    }
}
