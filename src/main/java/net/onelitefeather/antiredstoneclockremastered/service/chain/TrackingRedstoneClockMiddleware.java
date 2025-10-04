package net.onelitefeather.antiredstoneclockremastered.service.chain;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;

public final class TrackingRedstoneClockMiddleware extends RedstoneClockMiddleware {

    private final RedstoneTrackingService trackingService;
    private final AntiRedstoneClockRemastered antiRedstoneClockRemastered;

    @Inject
    public TrackingRedstoneClockMiddleware(@Named("staticTrackingService") RedstoneTrackingService trackingService,
                                           AntiRedstoneClockRemastered antiRedstoneClockRemastered) {
        this.trackingService = trackingService;
        this.antiRedstoneClockRemastered = antiRedstoneClockRemastered;
    }

    @Override
    public ResultState check(CheckContext context) {
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
