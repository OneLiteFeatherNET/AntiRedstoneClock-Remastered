package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.CheckContext;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.EventType;
import net.onelitefeather.antiredstoneclockremastered.service.tracking.StaticTrackingService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests which sculk vibrations reach the decision service and that they are counted by the tracking service.
 */
class SculkListenerTest {

    private static final int MAX_COUNT = 5;

    private final List<CheckContext> contexts = new ArrayList<>();
    private SculkListener listener;

    @BeforeEach
    void setUp() {
        DecisionService decisionService = new DecisionService() {
            @Override
            public void makeDecisionWithContext(CheckContext context) {
                contexts.add(context);
            }

            @Override
            public void reload() {
            }
        };
        this.listener = new SculkListener(decisionService);
    }

    private static Block blockOf(Material type) {
        var block = mock(Block.class);
        when(block.getType()).thenReturn(type);
        when(block.getLocation()).thenReturn(new Location(null, 2, 64, 2));
        return block;
    }

    /** A vibration caused by a block (piston, note block, ...) has no source entity. */
    private static BlockReceiveGameEvent blockCausedEvent(Material receiver) {
        var block = blockOf(receiver);
        var event = mock(BlockReceiveGameEvent.class);
        when(event.getBlock()).thenReturn(block);
        when(event.getEntity()).thenReturn(null);
        return event;
    }

    private void fire(BlockReceiveGameEvent event) throws Exception {
        var method = SculkListener.class.getDeclaredMethod("onBlockReceiveGameEvent", BlockReceiveGameEvent.class);
        method.setAccessible(true);
        method.invoke(this.listener, event);
    }

    @Test
    @DisplayName("A block caused vibration at a sculk sensor is reported as sculk sensor event")
    void sculkSensorEventIsReported() throws Exception {
        fire(blockCausedEvent(Material.SCULK_SENSOR));

        assertThat(this.contexts).hasSize(1);
        assertThat(this.contexts.get(0).eventType()).isEqualTo(EventType.SCULK_SENSOR);
        assertThat(this.contexts.get(0).state())
                .as("the sculk event is reported with a state, never as unknown")
                .isTrue();
    }

    @Test
    @DisplayName("A block caused vibration at a calibrated sculk sensor is reported")
    void calibratedSculkSensorEventIsReported() throws Exception {
        var calibrated = Material.getMaterial("CALIBRATED_SCULK_SENSOR");
        Assumptions.assumeTrue(calibrated != null, "needs a server API with calibrated sculk sensors");

        fire(blockCausedEvent(calibrated));

        assertThat(this.contexts).hasSize(1);
    }

    @Test
    @DisplayName("A vibration received by a sculk shrieker is ignored")
    void sculkShriekerEventIsIgnored() throws Exception {
        fire(blockCausedEvent(Material.SCULK_SHRIEKER));

        assertThat(this.contexts).as("shriekers also listen to vibrations but are not sensors").isEmpty();
    }

    @Test
    @DisplayName("A vibration caused by a player is ignored")
    void entityCausedEventIsIgnored() throws Exception {
        var event = blockCausedEvent(Material.SCULK_SENSOR);
        when(event.getEntity()).thenReturn(mock(Player.class));

        fire(event);

        assertThat(this.contexts).as("walking players are no self sustaining clock").isEmpty();
    }

    @Test
    @DisplayName("The handler opts out of cancelled events")
    void handlerIgnoresCancelledEvents() throws Exception {
        // Bukkit's event bus enforces ignoreCancelled; calling the method directly bypasses it. Paper
        // fires the event pre-cancelled when the sensor cannot receive the vibration, so the flag is
        // the contract and is asserted here instead of simulating the bus.
        var method = SculkListener.class.getDeclaredMethod("onBlockReceiveGameEvent", BlockReceiveGameEvent.class);
        var handler = method.getAnnotation(EventHandler.class);

        assertThat(handler).as("the handler must be an @EventHandler").isNotNull();
        assertThat(handler.ignoreCancelled())
                .as("a pre-cancelled event (sensor in cooldown) must never be counted")
                .isTrue();
        assertThat(handler.priority()).isEqualTo(EventPriority.LOWEST);
    }

    @Test
    @DisplayName("Every sculk activation counts towards the clock limit")
    void everyActivationCounts() throws Exception {
        var config = new YamlConfiguration();
        config.set("clock.maxCount", MAX_COUNT);
        config.set("clock.endDelay", 1_000_000);
        var plugin = mock(AntiRedstoneClockRemastered.class);
        when(plugin.getConfig()).thenReturn(config);
        var tracking = new StaticTrackingService(plugin);

        // The first event only starts the observation and the limit is checked before an event is
        // counted, exactly as for redstone, so the report follows after MAX_COUNT + 2 events. Counting
        // only every second event would need roughly twice as many.
        int events = 0;
        boolean clockDetected = false;
        while (!clockDetected && events < MAX_COUNT * 4) {
            fire(blockCausedEvent(Material.SCULK_SENSOR));
            events++;
            clockDetected = !this.contexts.isEmpty()
                    && tracking.isRedstoneClock(this.contexts.get(this.contexts.size() - 1));
        }

        assertThat(clockDetected).as("a sensor that activates repeatedly is a clock").isTrue();
        assertThat(events).as("every activation must count, not every second one").isEqualTo(MAX_COUNT + 2);
    }
}
