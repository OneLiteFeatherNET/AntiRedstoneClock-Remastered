package net.onelitefeather.antiredstoneclockremastered.service.api;

import org.bukkit.Location;

/**
 * Service for handling region-specific operations.
 *
 * @since 2.3.0-beta.10
 * @version 1.0.0
 * @author TheMeinerLP
 */
public interface RegionService {

    /**
     * Executes a task within the context of a specific region.
     *
     * @param location The location to determine the region.
     * @param task     The task to execute.
     */
    void executeInRegion(Location location, Runnable task);

    /**
     * Checks if the current server instance is the owner of the region at the specified location.
     *
     * @param location The location to check.
     * @return True if the server is the owner of the region, false otherwise.
     */
    boolean isRegionOwner(Location location);
}
