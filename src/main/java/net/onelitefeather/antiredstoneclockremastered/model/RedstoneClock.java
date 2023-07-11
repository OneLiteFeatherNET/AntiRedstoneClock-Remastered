package net.onelitefeather.antiredstoneclockremastered.model;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class RedstoneClock {

    /**
     * How often the clocks gets triggered
     */
    private int triggerCount = 0;

    /**
     * Shows if the piston/observer extended/enabled
     */
    private boolean active;
    private boolean detected;

    /**
     * Where's the clock located
     */
    private final @NotNull Location location;
    private final long endTime;

    public RedstoneClock(@NotNull Location location, long endTime) {
        this.location = location;
        this.endTime = endTime;
    }

    public void incrementTriggerCount() {
        this.triggerCount++;
    }

    public int getTriggerCount() {
        return triggerCount;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDetected() {
        return detected;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    public boolean isTimeOut() {
        return (System.currentTimeMillis() / 1000) >= endTime;
    }

    public Location getLocation() {
        return location;
    }
}
