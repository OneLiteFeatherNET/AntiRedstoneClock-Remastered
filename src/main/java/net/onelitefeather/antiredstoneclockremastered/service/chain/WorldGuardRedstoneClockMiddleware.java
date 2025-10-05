package net.onelitefeather.antiredstoneclockremastered.service.chain;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.api.WorldGuardSupport;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import org.jetbrains.annotations.NotNull;

public final class WorldGuardRedstoneClockMiddleware extends RedstoneClockMiddleware {

    private final WorldGuardSupport worldGuardSupport;

    @Inject
    public WorldGuardRedstoneClockMiddleware(WorldGuardSupport worldGuardSupport) {
        this.worldGuardSupport = worldGuardSupport;
    }

    @Override
    public @NotNull ResultState check(@NotNull CheckContext context) {
        if (this.worldGuardSupport.isRegionAllowed(context.location())) return ResultState.SKIP;
        return checkNext(context);
    }
}
