package net.onelitefeather.antiredstoneclockremastered.service.chain;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

public final class WorldRedstoneClockMiddleware extends RedstoneClockMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorldRedstoneClockMiddleware.class);

    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    @Inject
    public WorldRedstoneClockMiddleware(AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @Override
    public @NotNull ResultState check(@NotNull CheckContext context) {
        var ignoredWorlds = this.antiRedstoneClockRemastered.getConfig().getStringList("check.ignoredWorlds");
        var world = context.location().getWorld().getName();
        if (ignoredWorlds.contains(world)) {
            LOGGER.debug("Skipping {} check, world {} is ignored", context.eventType(), world);
            return ResultState.SKIP;
        }
        return checkNext(context);
    }
}
