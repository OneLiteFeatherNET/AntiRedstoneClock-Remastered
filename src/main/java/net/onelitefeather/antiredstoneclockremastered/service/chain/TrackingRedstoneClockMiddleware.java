package net.onelitefeather.antiredstoneclockremastered.service.chain;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;
import org.jetbrains.annotations.NotNull;

public final class TrackingRedstoneClockMiddleware extends RedstoneClockMiddleware {

    private final RedstoneTrackingService trackingService;
    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    @Inject
    public TrackingRedstoneClockMiddleware(RedstoneTrackingService trackingService,
                                           AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.trackingService = trackingService;
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @Override
    public @NotNull ResultState check(@NotNull CheckContext context) {
        if (this.trackingService.isRedstoneClock(context)) {
            var autoBreak = this.antiRedstoneClockRemastered.getConfig().getBoolean("clock.autoBreak", true);
            if (!autoBreak) return ResultState.ONLY_NOTIFY;
            var autoDrop = this.antiRedstoneClockRemastered.getConfig().getBoolean("clock.drop", false);
            if (autoDrop) return ResultState.REMOVE_AND_DROP;
            return ResultState.REMOVE_AND_WITHOUT_DROP;
        }
        return ResultState.SKIP;
    }
}
