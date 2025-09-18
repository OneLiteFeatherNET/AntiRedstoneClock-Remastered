package net.onelitefeather.antiredstoneclockremastered.service.api;

import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Service interface for managing redstone clock detection and handling.
 * This abstraction allows for different implementations (e.g., Bukkit, Folia).
 *
 * @author TheMeinerLP
 * @version 2.2.0
 * @since 1.0.0
 */
public interface RedstoneClockService {

    /**
     * Check and update clock state with manual active state control.
     *
     * @param location the location to check
     * @param state the active state to set
     */
    void checkAndUpdateClockStateWithActiveManual(@NotNull Location location, boolean state);

    /**
     * Check and update clock state with manual active state control.
     *
     * @param block the block to check
     * @param state the active state to set
     */
    void checkAndUpdateClockStateWithActiveManual(@NotNull Block block, boolean state);

    /**
     * Check and update clock state with automatic active state detection.
     *
     * @param block the block to check
     */
    void checkAndUpdateClockStateWithActive(@NotNull Block block);

    /**
     * Check and update clock state with automatic active state detection.
     *
     * @param location the location to check
     */
    void checkAndUpdateClockStateWithActive(@NotNull Location location);

    /**
     * Check and update clock state without active state management.
     *
     * @param block the block to check
     */
    void checkAndUpdateClockState(@NotNull Block block);

    /**
     * Check and update clock state without active state management.
     *
     * @param location the location to check
     */
    void checkAndUpdateClockState(@NotNull Location location);

    /**
     * Add a new redstone clock test at the specified location.
     *
     * @param location the location to test
     */
    void addRedstoneClockTest(@NotNull Location location);

    /**
     * Reload the service configuration.
     */
    void reload();

    /**
     * Remove a clock by its location.
     *
     * @param location the location of the clock to remove
     */
    void removeClockByLocation(@NotNull Location location);

    /**
     * Remove a clock by the clock object itself.
     *
     * @param redstoneClock the clock to remove
     */
    void removeClockByClock(@NotNull RedstoneClock redstoneClock);

    /**
     * Check if the service contains a clock at the specified location.
     *
     * @param location the location to check
     * @return true if a clock exists at the location, false otherwise
     */
    boolean containsLocation(@NotNull Location location);

    /**
     * Get the clock at the specified location.
     *
     * @param location the location to check
     * @return the clock at the location, or null if none exists
     */
    @Nullable
    RedstoneClock getClockByLocation(@NotNull Location location);

    /**
     * Get all active redstone clocks.
     *
     * @return an unmodifiable collection of all redstone clocks
     */
    @NotNull
    Collection<RedstoneClock> getRedstoneClocks();

    /**
     * Get all locations with active redstone clocks.
     *
     * @return an unmodifiable collection of all clock locations
     */
    @NotNull
    Collection<Location> getRedstoneClockLocations();

    /**
     * Get all active clock testers as a map.
     *
     * @return a copy of the active testers map
     */
    @NotNull
    Map<Location, RedstoneClock> getActiveTester();
}