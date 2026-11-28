package net.onelitefeather.antiredstoneclockremastered.service.tracking;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.model.DynamicRedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.CheckContext;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.EventType;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.List;
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
    private static final Location MOVED_LOCATION = new Location(null, 5, 64, 1);

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
        return trigger(service, CLOCK_LOCATION);
    }

    private static boolean trigger(DynamicTrackingService service, Location location) {
        return service.isRedstoneClock(CheckContext.of(location, null, null, EventType.REDSTONE_AND_REPEATER));
    }

    private static boolean trigger(DynamicTrackingService service, EventType eventType) {
        return service.isRedstoneClock(CheckContext.of(CLOCK_LOCATION, null, true, eventType));
    }

    private static DynamicRedstoneClock onlyClock(DynamicTrackingService service) {
        assertThat(service.getRedstoneClocks()).as("exactly one tracked clock").hasSize(1);
        return (DynamicRedstoneClock) service.getRedstoneClocks().iterator().next();
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

    @Test
    @DisplayName("The marker of a tracked clock is kept and its triggers accumulate")
    void trackedMarkerIsKeptAndTriggersAccumulate() {
        var service = createService(LONG_END_DELAY);
        trigger(service);
        var marker = this.markers.read(CLOCK_LOCATION);

        for (int signal = 0; signal < MAX_COUNT - 2; signal++) {
            trigger(service);
        }

        assertThat(this.markers.read(CLOCK_LOCATION))
                .as("the marker of a tracked clock must not be overwritten")
                .isEqualTo(marker);
        assertThat(onlyClock(service).getTriggerCount())
                .as("every signal after the first must be added to the same clock")
                .isEqualTo(MAX_COUNT - 2);
    }

    @Test
    @DisplayName("A moved clock that reaches the limit removes the marker at its current location")
    void movedClockReachingLimitRemovesMarkerAtCurrentLocation() {
        var service = createService(LONG_END_DELAY);
        trigger(service);
        var clock = onlyClock(service);
        moveClock(clock);

        assertThat(triggerUntilDetected(service, MOVED_LOCATION, MAX_COUNT * 4))
                .as("the moved clock must be reported as clock")
                .isTrue();
        assertThat(this.markers.read(MOVED_LOCATION))
                .as("the marker at the current location of the clock must be removed")
                .isNull();
    }

    @Test
    @DisplayName("Removing a moved clock removes the marker at its current location")
    void removingMovedClockRemovesMarkerAtCurrentLocation() {
        var service = createService(LONG_END_DELAY);
        trigger(service);
        var clock = onlyClock(service);
        moveClock(clock);

        service.removeClockByClock(clock);

        assertThat(this.markers.read(MOVED_LOCATION))
                .as("the marker at the current location of the clock must be removed")
                .isNull();
        assertThat(service.getRedstoneClocks())
                .as("the removed clock must no longer be tracked")
                .isEmpty();
    }

    @ParameterizedTest(name = "{0} counts every report")
    @EnumSource(value = EventType.class, names = {"REDSTONE_AND_REPEATER", "HOPPER", "SCULK_SENSOR"})
    @DisplayName("Signal like events count every report")
    void signalEventsCountEveryReport(EventType eventType) {
        var service = createService(LONG_END_DELAY);

        for (int report = 0; report < 4; report++) {
            trigger(service, eventType);
        }

        assertThat(onlyClock(service).getTriggerCount())
                .as("%s must count every report after the first", eventType)
                .isEqualTo(3);
    }

    @ParameterizedTest(name = "{0} counts every second report")
    @EnumSource(value = EventType.class, names = {"OBSERVER", "COMPARATOR", "PISTON"})
    @DisplayName("State based events count every second report")
    void stateEventsCountEverySecondReport(EventType eventType) {
        var service = createService(LONG_END_DELAY);

        for (int report = 0; report < 7; report++) {
            trigger(service, eventType);
        }

        assertThat(onlyClock(service).getTriggerCount())
                .as("%s must count one trigger per on and off cycle after the first report", eventType)
                .isEqualTo(3);
    }

    @Test
    @DisplayName("After a restart every marked block is tracked again on its own")
    void everyMarkedBlockIsTrackedAgainAfterRestart() {
        var locations = List.of(CLOCK_LOCATION, MOVED_LOCATION, new Location(null, 9, 70, -3));
        locations.forEach(location -> this.markers.write(location, UUID.randomUUID()));
        var service = createService(LONG_END_DELAY);

        var detected = new HashMap<Location, Boolean>();
        for (int round = 0; round < MAX_COUNT * 4; round++) {
            for (var location : locations) {
                if (trigger(service, location)) {
                    detected.put(location, true);
                }
            }
        }

        assertThat(detected.keySet())
                .as("every block with a marker from before the restart must be reported as clock")
                .containsExactlyInAnyOrderElementsOf(locations);
    }

    @Test
    @DisplayName("A timed out clock is no longer listed")
    void timedOutClockIsNoLongerListed() {
        var service = createService(-1);
        trigger(service);
        var timedOut = onlyClock(service);

        trigger(service);

        assertThat(service.getRedstoneClocks())
                .as("the timed out clock must not be listed anymore")
                .doesNotContain(timedOut);
    }

    @Test
    @DisplayName("A detected clock is no longer listed")
    void detectedClockIsNoLongerListed() {
        var service = createService(LONG_END_DELAY);
        trigger(service);
        var detected = onlyClock(service);

        triggerUntilDetected(service, MAX_COUNT * 4);

        assertThat(service.getRedstoneClocks())
                .as("the detected clock must not be listed anymore")
                .doesNotContain(detected);
    }

    /**
     * Moves the clock like a flying machine does: the marker travels with the block.
     */
    private void moveClock(DynamicRedstoneClock clock) {
        clock.setCurrentLocation(MOVED_LOCATION);
        this.markers.remove(CLOCK_LOCATION);
        this.markers.write(MOVED_LOCATION, clock.getUuid());
    }

    private static boolean triggerUntilDetected(DynamicTrackingService service, Location location, int attempts) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            if (trigger(service, location)) {
                return true;
            }
        }
        return false;
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
