package net.onelitefeather.antiredstoneclockremastered.service.tracking;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.model.StaticRedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Named("staticTrackingService")
public final class StaticTrackingService implements RedstoneTrackingService {

    private final ConcurrentHashMap<Location, StaticRedstoneClock> activeClockTesters = new ConcurrentHashMap<>();

    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    @Inject
    public StaticTrackingService(AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
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

    private boolean expireOrDestroyIfNeeded(@NotNull StaticRedstoneClock clock) {
        if (clock.isTimeOut()) {
            removeClockByClock(clock);
            return false;
        }
        if (clock.getTriggerCount() >= antiRedstoneClockRemastered.getConfig().getInt("clock.maxCount", 150)) {
            removeClockByClock(clock);
            return true;
        }
        return false;
    }

    public void addRedstoneClockTest(@NotNull Location location) {
        this.activeClockTesters.putIfAbsent(location, new StaticRedstoneClock(location, (System.currentTimeMillis() / 1000) + antiRedstoneClockRemastered.getConfig().getInt("clock.endDelay", 300)));
    }


    public void removeClockByLocation(@NotNull Location location) {
        this.activeClockTesters.remove(location);
    }


    public void removeClockByClock(@NotNull StaticRedstoneClock staticRedstoneClock) {
        removeClockByLocation(staticRedstoneClock.getLocation());
    }


    public boolean containsLocation(@NotNull Location location) {
        return this.activeClockTesters.containsKey(location);
    }


    @Nullable
    public StaticRedstoneClock getClockByLocation(@NotNull Location location) {
        return this.activeClockTesters.get(location);
    }


    @Override
    @NotNull
    public Collection<RedstoneClock> getRedstoneClocks() {
        return Collections.unmodifiableCollection(this.activeClockTesters.values());
    }


    @NotNull
    public Collection<Location> getRedstoneClockLocations() {
        return Collections.unmodifiableCollection(this.activeClockTesters.keySet());
    }

    @NotNull
    public Map<Location, StaticRedstoneClock> getActiveTester() {
        return Map.copyOf(this.activeClockTesters);
    }
}
