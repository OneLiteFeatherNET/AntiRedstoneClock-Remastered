package net.onelitefeather.antiredstoneclockremastered.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DynamicRedstoneClock implements RedstoneClock {

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
    private Location currentLocation;
    private final long endTime;
    private final UUID uuid;

    public DynamicRedstoneClock(@NotNull Location currentLocation, long endTime) {
        this.currentLocation = currentLocation;
        this.endTime = endTime;
        this.uuid = UUID.randomUUID();
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

    @NotNull
    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(@NotNull Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public UUID getUuid() {
        return uuid;
    }



    @Override
    public Component render() {
        var location = getCurrentLocation();
        return Component.empty().hoverEvent(Component.translatable("antiredstoneclockremastered.command.display.clock.hover").asHoverEvent()).append(
                Component.translatable("antiredstoneclockremastered.command.display.clock.text")
                        .arguments(
                                TranslationArgument.numeric(getTriggerCount()),
                                TranslationArgument.numeric(location.getBlockX()),
                                TranslationArgument.numeric(location.getBlockY()),
                                TranslationArgument.numeric(location.getBlockZ()),
                                Component.empty()
                        ).clickEvent(ClickEvent.callback(audience -> {
                            if (audience instanceof final Player executor) {
                                executor.teleport(location);
                            }
                        }))
        );
    }
}
