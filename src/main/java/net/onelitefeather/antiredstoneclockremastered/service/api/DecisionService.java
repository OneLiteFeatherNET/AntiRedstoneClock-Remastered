package net.onelitefeather.antiredstoneclockremastered.service.api;

/**
 * Service interface for managing redstone clock detection and handling.
 * This abstraction allows for different implementations (e.g., Bukkit, Folia).
 *
 * @author TheMeinerLP
 * @version 2.3.0
 * @since 1.0.0
 */
public interface DecisionService {

    /**
     * Check the state of the clock at the specified block.
     * @param context the check context
     */
    void makeDecisionWithContext(RedstoneClockMiddleware.CheckContext context);

    /**
     * Reload the service configuration.
     */
    void reload();
}