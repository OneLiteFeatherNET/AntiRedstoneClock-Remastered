package net.onelitefeather.antiredstoneclockremastered.service.tracking;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.CheckContext;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.EventType;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that the dynamic tracking service keeps its block markers in line with the tracked clocks.
 */
class DynamicTrackingServiceTest {

    private static final int MAX_COUNT = 5;
    private static final int LONG_END_DELAY = 1_000_000;
    private static final Location CLOCK_LOCATION = new Location(null, 1, 64, 1);

    private final InMemoryMarkers markers = new InMemoryMarkers();

    private DynamicTrackingService createService(int endDelay) {
        var config = new YamlConfiguration();
        config.set("clock.maxCount", MAX_COUNT);
        config.set("clock.endDelay", endDelay);
        var plugin = mock(AntiRedstoneClockRemastered.class);
        when(plugin.getConfig()).thenReturn(config);
        return new DynamicTrackingService(plugin, this.markers);
    }

    private static boolean trigger(DynamicTrackingService service) {
        return service.isRedstoneClock(CheckContext.of(CLOCK_LOCATION, null, null, EventType.REDSTONE_AND_REPEATER));
    }

    private static boolean triggerUntilDetected(DynamicTrackingService service, int attempts) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            if (trigger(service)) {
                return true;
            }
        }
        return false;
    }

    @Test
    @DisplayName("A marker left over without a tracked clock does not block tracking")
    void orphanedMarkerIsTrackedAgain() {
        this.markers.write(CLOCK_LOCATION, UUID.randomUUID());
        var service = createService(LONG_END_DELAY);

        assertThat(triggerUntilDetected(service, MAX_COUNT * 4))
                .as("a block with an orphaned marker must be reported as clock after %s triggers", MAX_COUNT)
                .isTrue();
    }

    @Test
    @DisplayName("An orphaned marker is overwritten with the uuid of the new clock")
    void orphanedMarkerIsOverwritten() {
        var orphan = UUID.randomUUID();
        this.markers.write(CLOCK_LOCATION, orphan);
        var service = createService(LONG_END_DELAY);

        trigger(service);

        assertThat(service.getRedstoneClocks())
                .as("the block must be tracked again")
                .hasSize(1);
        assertThat(this.markers.read(CLOCK_LOCATION))
                .as("the marker must point to the new clock instead of the orphan")
                .isNotNull()
                .isNotEqualTo(orphan);
    }

    @Test
    @DisplayName("A block is counted again after its clock timed out")
    void timedOutBlockIsCountedAgain() {
        var service = createService(-1);
        trigger(service);
        var firstMarker = this.markers.read(CLOCK_LOCATION);

        trigger(service);

        assertThat(service.getRedstoneClocks())
                .as("the timed out clock must be replaced by a new one")
                .hasSize(1);
        assertThat(this.markers.read(CLOCK_LOCATION))
                .as("the marker must point to the new clock")
                .isNotNull()
                .isNotEqualTo(firstMarker);
    }

    @Test
    @DisplayName("The marker is removed once the trigger limit is reached")
    void markerIsRemovedWhenLimitIsReached() {
        var service = createService(LONG_END_DELAY);

        assertThat(triggerUntilDetected(service, MAX_COUNT * 4))
                .as("the block must be reported as clock")
                .isTrue();
        assertThat(this.markers.read(CLOCK_LOCATION))
                .as("the marker of the detected clock must be removed")
                .isNull();
        assertThat(service.getRedstoneClocks())
                .as("the detected clock must no longer be tracked")
                .isEmpty();
    }

    @Test
    @DisplayName("A block is detected again after an earlier detection")
    void blockIsDetectedAgainAfterDetection() {
        var service = createService(LONG_END_DELAY);
        assertThat(triggerUntilDetected(service, MAX_COUNT * 4))
                .as("the first detection")
                .isTrue();

        assertThat(triggerUntilDetected(service, MAX_COUNT * 4))
                .as("a clock that keeps running after its detection must be reported again")
                .isTrue();
    }

    @Test
    @DisplayName("Removing a clock by location removes its marker")
    void removalByLocationRemovesMarker() {
        var service = createService(LONG_END_DELAY);
        trigger(service);

        service.removeClockByLocation(CLOCK_LOCATION);

        assertThat(this.markers.read(CLOCK_LOCATION))
                .as("the marker of the removed clock must be gone")
                .isNull();
        assertThat(service.getRedstoneClocks())
                .as("the removed clock must no longer be tracked")
                .isEmpty();
    }

    private static final class InMemoryMarkers implements DynamicTrackingService.ClockMarkers {

        private final Map<Location, UUID> markers = new HashMap<>();

        @Override
        public @Nullable UUID read(@NotNull Location location) {
            return this.markers.get(location);
        }

        @Override
        public void write(@NotNull Location location, @NotNull UUID uuid) {
            this.markers.put(location, uuid);
        }

        @Override
        public void remove(@NotNull Location location) {
            this.markers.remove(location);
        }
    }
}
