package net.onelitefeather.antiredstoneclockremastered.service.chain;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import org.jetbrains.annotations.NotNull;

public final class SkipEventTypeRedstoneClockMiddleware extends RedstoneClockMiddleware {

    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    @Inject
    public SkipEventTypeRedstoneClockMiddleware(AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @Override
    public @NotNull ResultState check(@NotNull CheckContext context) {
        if (context.eventType() == EventType.SCULK_SENSOR &&
                this.antiRedstoneClockRemastered.getConfig().getBoolean("check.sculkSensor"))
            return checkNext(context);
        if (context.eventType() == EventType.REDSTONE_AND_REPEATER &&
                this.antiRedstoneClockRemastered.getConfig().getBoolean("check.redstoneAndRepeater"))
            return checkNext(context);
        if (context.eventType() == EventType.PISTON &&
                this.antiRedstoneClockRemastered.getConfig().getBoolean("check.piston"))
            return checkNext(context);
        if (context.eventType() == EventType.OBSERVER &&
                this.antiRedstoneClockRemastered.getConfig().getBoolean("check.observer"))
            return checkNext(context);
        if (context.eventType() == EventType.COMPARATOR &&
                this.antiRedstoneClockRemastered.getConfig().getBoolean("check.comparator"))
            return checkNext(context);
        return ResultState.SKIP;
    }
}
