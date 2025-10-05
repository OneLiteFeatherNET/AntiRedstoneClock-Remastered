package net.onelitefeather.antiredstoneclockremastered.service.chain;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import org.jetbrains.annotations.NotNull;

public final class WorldRedstoneClockMiddleware extends RedstoneClockMiddleware {

    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    @Inject
    public WorldRedstoneClockMiddleware(AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @Override
    public @NotNull ResultState check(@NotNull CheckContext context) {
        var ignoredWorlds = this.antiRedstoneClockRemastered.getConfig().getStringList("check.ignoredWorlds");
        if (ignoredWorlds.contains(context.location().getWorld().getName())) return ResultState.SKIP;
        return checkNext(context);
    }
}
