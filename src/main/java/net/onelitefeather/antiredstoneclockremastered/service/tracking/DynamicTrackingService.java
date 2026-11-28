package net.onelitefeather.antiredstoneclockremastered.service.tracking;

import com.jeff_media.customblockdata.CustomBlockData;
import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.model.DynamicRedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;
import net.onelitefeather.antiredstoneclockremastered.utils.UUIDTagType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.metadata.FixedMetadataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DynamicTrackingService implements RedstoneTrackingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTrackingService.class);
    private static final NamespacedKey REDSTONE_CLOCK_METADATA_KEY = NamespacedKey.fromString("antiredstoneclock:redstone_clock");
    private final ConcurrentHashMap<UUID, DynamicRedstoneClock> activeClockTesters = new ConcurrentHashMap<>();
    private final AntiRedstoneClockRemastered plugin;
    private final ClockMarkers clockMarkers;

    @Inject
    public DynamicTrackingService(AntiRedstoneClockRemastered plugin) {
        this(plugin, new BlockDataClockMarkers(plugin));
    }

    DynamicTrackingService(AntiRedstoneClockRemastered plugin, ClockMarkers clockMarkers) {
        this.plugin = plugin;
        this.clockMarkers = clockMarkers;
    }

    @Override
    public boolean isRedstoneClock(RedstoneClockMiddleware.@NotNull CheckContext context) {
        var eventType = context.eventType();
        var location = context.location();
        final boolean desiredState = Optional.ofNullable(context.state()).orElse(false);
        var clock = getClockByLocation(location);
        if (clock != null) {
            if (expireOrDestroyIfNeeded(clock)) return true;

            // The hopper listener only reports completed back and forth cycles and the sculk
            // listener only reports vibrations the sensor accepted, so every report counts
            // as a trigger just like a redstone signal does.
            if (eventType == RedstoneClockMiddleware.EventType.REDSTONE_AND_REPEATER
                    || eventType == RedstoneClockMiddleware.EventType.HOPPER
                    || eventType == RedstoneClockMiddleware.EventType.SCULK_SENSOR) {
                clock.incrementTriggerCount();
                clock.setCurrentLocation(location);
            } else {
                if (clock.isActive()) {
                    clock.incrementTriggerCount();
                    clock.setCurrentLocation(location);
                    clock.setActive(false);
                    return false;
                } else {
                    clock.setActive(desiredState);
                }
            }
        }
        addRedstoneClockTest(location);
        return false;
    }

    private boolean expireOrDestroyIfNeeded(@NotNull DynamicRedstoneClock clock) {
        if (clock.isTimeOut()) {
            LOGGER.debug("Tracked block at {} timed out after {} triggers",
                    clock.getCurrentLocation(), clock.getTriggerCount());
            removeClockByClock(clock);
            return false;
        }
        if (clock.getTriggerCount() >= plugin.getConfig().getInt("clock.maxCount", 150)) {
            LOGGER.debug("Block at {} reached the trigger limit with {} triggers",
                    clock.getCurrentLocation(), clock.getTriggerCount());
            removeClockByClock(clock);
            return true;
        }
        return false;
    }

    public void removeClockByClock(@NotNull DynamicRedstoneClock staticRedstoneClock) {
        removeClockByLocation(staticRedstoneClock.getCurrentLocation());
    }

    private DynamicRedstoneClock getClockByLocation(Location location) {
        var uuid = this.clockMarkers.read(location);
        if (uuid != null) {
            return this.activeClockTesters.get(uuid);
        }
        return null;
    }

    public void addRedstoneClockTest(@NotNull Location location) {
        var uuid = this.clockMarkers.read(location);
        // A marker without a tracked clock is left over from a removed clock or a restart and is overwritten.
        if (uuid != null && this.activeClockTesters.containsKey(uuid)) {
            return;
        }
        var dynamicRedstoneClock = new DynamicRedstoneClock(location, (System.currentTimeMillis() / 1000) + plugin.getConfig().getInt("clock.endDelay", 300));
        this.clockMarkers.write(location, dynamicRedstoneClock.getUuid());
        this.activeClockTesters.put(dynamicRedstoneClock.getUuid(), dynamicRedstoneClock);
    }


    @Override
    public Collection<RedstoneClock> getRedstoneClocks() {
        return Collections.unmodifiableCollection(this.activeClockTesters.values());
    }

    @Override
    public void removeClockByLocation(Location location) {
        this.activeClockTesters.values().removeIf(dynamicRedstoneClock -> dynamicRedstoneClock.getCurrentLocation().equals(location));
        this.clockMarkers.remove(location);
    }

    /**
     * Reads, writes and removes the marker that links a block to its tracked clock.
     */
    interface ClockMarkers {

        @Nullable
        UUID read(@NotNull Location location);

        void write(@NotNull Location location, @NotNull UUID uuid);

        void remove(@NotNull Location location);
    }

    private record BlockDataClockMarkers(AntiRedstoneClockRemastered plugin) implements ClockMarkers {

        @Override
        public @Nullable UUID read(@NotNull Location location) {
            return new CustomBlockData(location.getBlock(), this.plugin).get(REDSTONE_CLOCK_METADATA_KEY, UUIDTagType.UUID);
        }

        @Override
        public void write(@NotNull Location location, @NotNull UUID uuid) {
            new CustomBlockData(location.getBlock(), this.plugin).set(REDSTONE_CLOCK_METADATA_KEY, UUIDTagType.UUID, uuid);
        }

        @Override
        public void remove(@NotNull Location location) {
            new CustomBlockData(location.getBlock(), this.plugin).remove(REDSTONE_CLOCK_METADATA_KEY);
        }
    }
}
