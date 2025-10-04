package net.onelitefeather.antiredstoneclockremastered.service.tracking;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;
import org.bukkit.Location;

import java.util.Collection;
import java.util.List;

public final class DelegatedTrackingService implements RedstoneTrackingService {

    private final RedstoneTrackingService staticTrackingService;

    @Inject
    public DelegatedTrackingService(@Named("staticTrackingService") RedstoneTrackingService staticTrackingService) {
        this.staticTrackingService = staticTrackingService;
    }

    @Override
    public boolean isRedstoneClock(RedstoneClockMiddleware.CheckContext context) {
        return this.staticTrackingService.isRedstoneClock(context);
    }

    @Override
    public Collection<RedstoneClock> getRedstoneClocks() {
        return List.copyOf(this.staticTrackingService.getRedstoneClocks());
    }

    @Override
    public void removeClockByLocation(Location location) {
        this.staticTrackingService.removeClockByLocation(location);
    }
}
