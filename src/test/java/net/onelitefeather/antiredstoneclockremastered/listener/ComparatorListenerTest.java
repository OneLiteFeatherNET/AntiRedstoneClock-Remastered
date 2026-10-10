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
import org.bukkit.event.block.BlockRedstoneEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that comparator updates reach the decision service in a form the tracking service can count.
 */
class ComparatorListenerTest {

    private static final int MAX_COUNT = 5;

    private final List<CheckContext> contexts = new ArrayList<>();
    private Block comparator;
    private ComparatorListener listener;

    @BeforeEach
    void setUp() {
        this.comparator = mock(Block.class);
        when(this.comparator.getType()).thenReturn(Material.COMPARATOR);
        when(this.comparator.getLocation()).thenReturn(new Location(null, 1, 64, 1));
        DecisionService decisionService = new DecisionService() {
            @Override
            public void makeDecisionWithContext(CheckContext context) {
                contexts.add(context);
            }

            @Override
            public void reload() {
            }
        };
        this.listener = new ComparatorListener(Material.COMPARATOR, decisionService);
    }

    private void fire(int oldCurrent, int newCurrent) throws Exception {
        var method = ComparatorListener.class.getDeclaredMethod("onRedstoneComparatorClock", BlockRedstoneEvent.class);
        method.setAccessible(true);
        method.invoke(this.listener, new BlockRedstoneEvent(this.comparator, oldCurrent, newCurrent));
    }

    @Test
    @DisplayName("A rising comparator edge is reported as active")
    void risingEdgeIsReportedAsActive() throws Exception {
        fire(0, 15);

        assertThat(this.contexts).hasSize(1);
        assertThat(this.contexts.get(0).eventType()).isEqualTo(EventType.COMPARATOR);
        assertThat(this.contexts.get(0).state())
                .as("the tracking service needs a state to arm a clock")
                .isTrue();
    }

    @Test
    @DisplayName("A falling comparator edge is ignored")
    void fallingEdgeIsIgnored() throws Exception {
        fire(15, 0);

        assertThat(this.contexts).as("no decision for a falling edge").isEmpty();
    }

    @Test
    @DisplayName("Repeated comparator edges on one block reach the clock limit")
    void repeatedComparatorEdgesReachTheClockLimit() throws Exception {
        var config = new YamlConfiguration();
        config.set("clock.maxCount", MAX_COUNT);
        config.set("clock.endDelay", 1_000_000);
        var plugin = mock(AntiRedstoneClockRemastered.class);
        when(plugin.getConfig()).thenReturn(config);
        var tracking = new StaticTrackingService(plugin);

        boolean clockDetected = false;
        for (int edge = 0; edge < MAX_COUNT * 4 && !clockDetected; edge++) {
            fire(0, 15);
            clockDetected = tracking.isRedstoneClock(this.contexts.get(this.contexts.size() - 1));
        }

        assertThat(clockDetected)
                .as("a comparator that toggles %s times must be reported as a clock", MAX_COUNT * 4)
                .isTrue();
    }
}
