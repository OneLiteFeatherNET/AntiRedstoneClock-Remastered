package net.onelitefeather.antiredstoneclockremastered.service.tracking;

import com.jeff_media.customblockdata.CustomBlockData;
import jakarta.inject.Inject;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.model.DynamicRedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;
import net.onelitefeather.antiredstoneclockremastered.utils.UUIDTagType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DynamicTrackingService implements RedstoneTrackingService {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(DynamicTrackingService.class);
    private static final NamespacedKey REDSTONE_CLOCK_METADATA_KEY = NamespacedKey.fromString("antiredstoneclock:redstone_clock");
    private final ConcurrentHashMap<UUID, DynamicRedstoneClock> activeClockTesters = new ConcurrentHashMap<>();
    private final AntiRedstoneClockRemastered plugin;

    @Inject
    public DynamicTrackingService(AntiRedstoneClockRemastered plugin) {
        this.plugin = plugin;
        CustomBlockData.registerListener(plugin);
    }

    @Override
    public boolean isRedstoneClock(RedstoneClockMiddleware.@NotNull CheckContext context) {
        var eventType = context.eventType();
        var location = context.location();
        final boolean desiredState = Optional.ofNullable(context.state()).orElse(false);
        var clock = getClockByLocation(location);
        if (clock != null) {
            if (expireOrDestroyIfNeeded(clock)) return true;

            if (eventType == RedstoneClockMiddleware.EventType.REDSTONE_AND_REPEATER) {
                clock.incrementTriggerCount();
            } else {
                if (clock.isActive()) {
                    clock.incrementTriggerCount();
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
            removeClockByClock(clock);
            return false;
        }
        if (clock.getTriggerCount() >= plugin.getConfig().getInt("clock.maxCount", 150)) {
            removeClockByClock(clock);
            return true;
        }
        return false;
    }

    public void removeClockByClock(@NotNull DynamicRedstoneClock staticRedstoneClock) {
        removeClockByLocation(staticRedstoneClock.getCurrentLocation());
    }

    private DynamicRedstoneClock getClockByLocation(Location location) {
        var block = location.getBlock();
        var customBlockData = new CustomBlockData(block, this.plugin);
        if (customBlockData.has(REDSTONE_CLOCK_METADATA_KEY)) {
            var uuid = customBlockData.get(REDSTONE_CLOCK_METADATA_KEY, UUIDTagType.UUID);
            return this.activeClockTesters.get(uuid);
        }
        return null;
    }

    public void addRedstoneClockTest(@NotNull Location location) {
        var block = location.getBlock();
        var customBlockData = new CustomBlockData(block, this.plugin);
        if (customBlockData.has(REDSTONE_CLOCK_METADATA_KEY)) {
            return;
        }
        var dynamicRedstoneClock = new DynamicRedstoneClock(location, (System.currentTimeMillis() / 1000) + plugin.getConfig().getInt("clock.endDelay", 300));
        customBlockData.set(REDSTONE_CLOCK_METADATA_KEY, UUIDTagType.UUID, dynamicRedstoneClock.getUuid());
        this.activeClockTesters.put(dynamicRedstoneClock.getUuid(), dynamicRedstoneClock);
    }


    @Override
    public Collection<RedstoneClock> getRedstoneClocks() {
        return Collections.unmodifiableCollection(this.activeClockTesters.values());
    }

    @Override
    public void removeClockByLocation(Location location) {
        this.activeClockTesters.values().removeIf(dynamicRedstoneClock -> dynamicRedstoneClock.getCurrentLocation().equals(location));
    }
}
