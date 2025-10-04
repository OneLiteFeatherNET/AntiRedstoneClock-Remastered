package net.onelitefeather.antiredstoneclockremastered.service.api;

import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.model.StaticRedstoneClock;
import org.bukkit.Location;

import java.util.Collection;

/**
 * Service for tracking redstone signals.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 2.4.0
 */
public interface RedstoneTrackingService {

    boolean isRedstoneClock(RedstoneClockMiddleware.CheckContext context);

    Collection<RedstoneClock> getRedstoneClocks();

    void removeClockByLocation(Location location);

}
