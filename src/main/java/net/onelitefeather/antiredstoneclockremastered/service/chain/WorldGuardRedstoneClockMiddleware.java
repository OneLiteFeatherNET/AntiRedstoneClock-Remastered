package net.onelitefeather.antiredstoneclockremastered.service.chain;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.api.WorldGuardSupport;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

public final class WorldGuardRedstoneClockMiddleware extends RedstoneClockMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorldGuardRedstoneClockMiddleware.class);

    private final WorldGuardSupport worldGuardSupport;

    @Inject
    public WorldGuardRedstoneClockMiddleware(WorldGuardSupport worldGuardSupport) {
        this.worldGuardSupport = worldGuardSupport;
    }

    @Override
    public @NotNull ResultState check(@NotNull CheckContext context) {
        if (this.worldGuardSupport.isRegionAllowed(context.location())) {
            LOGGER.debug("Skipping {} check, a WorldGuard region allows redstone clocks at {}",
                    context.eventType(), context.location());
            return ResultState.SKIP;
        }
        return checkNext(context);
    }
}
