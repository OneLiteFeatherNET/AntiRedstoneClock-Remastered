package net.onelitefeather.antiredstoneclockremastered.service.tracking;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;
import org.bukkit.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class DelegatedTrackingService implements RedstoneTrackingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedTrackingService.class);
    private final RedstoneTrackingService staticTrackingService;
    private final RedstoneTrackingService dynamicTrackingService;
    private final AntiRedstoneClockRemastered plugin;

    @Inject
    public DelegatedTrackingService(@Named("staticTrackingService") RedstoneTrackingService staticTrackingService,
                                    @Named("dynamicTrackingService") RedstoneTrackingService dynamicTrackingService,
                                    AntiRedstoneClockRemastered plugin) {
        this.staticTrackingService = staticTrackingService;
        this.dynamicTrackingService = dynamicTrackingService;
        this.plugin = plugin;
    }

    @Override
    public boolean isRedstoneClock(RedstoneClockMiddleware.CheckContext context) {
        var mode = ConfigMode.getEnum(this.plugin.getConfig(), "check.mode", ConfigMode.STATIC);
        return switch (mode) {
            case STATIC -> this.staticTrackingService.isRedstoneClock(context);
            case DYNAMIC -> this.dynamicTrackingService.isRedstoneClock(context);
        };
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
