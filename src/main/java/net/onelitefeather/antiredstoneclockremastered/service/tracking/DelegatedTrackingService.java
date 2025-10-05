package net.onelitefeather.antiredstoneclockremastered.service.tracking;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class DelegatedTrackingService implements RedstoneTrackingService {

    private final RedstoneTrackingService staticTrackingService;
    private final RedstoneTrackingService dynamicTrackingService;

    @Inject
    public DelegatedTrackingService(@Named("staticTrackingService") RedstoneTrackingService staticTrackingService,
                                    @Named("dynamicTrackingService") RedstoneTrackingService dynamicTrackingService) {
        this.staticTrackingService = staticTrackingService;
        this.dynamicTrackingService = dynamicTrackingService;
    }

    @Override
    public boolean isRedstoneClock(RedstoneClockMiddleware.CheckContext context) {
        return this.staticTrackingService.isRedstoneClock(context);
//        return this.staticTrackingService.isRedstoneClock(context) || this.dynamicTrackingService.isRedstoneClock(context);
    }

    @Override
    public Collection<RedstoneClock> getRedstoneClocks() {
        List<RedstoneClock> redstoneClocks = new ArrayList<>(this.dynamicTrackingService.getRedstoneClocks());
        redstoneClocks.addAll(this.staticTrackingService.getRedstoneClocks());
        return redstoneClocks;
    }

    @Override
    public void removeClockByLocation(Location location) {
        this.staticTrackingService.removeClockByLocation(location);
        this.dynamicTrackingService.removeClockByLocation(location);
    }
}
