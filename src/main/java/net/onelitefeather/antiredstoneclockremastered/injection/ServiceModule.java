package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.papermc.paper.ServerBuildInfo;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.api.WorldGuardSupport;
import net.onelitefeather.antiredstoneclockremastered.service.api.NotificationService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.service.factory.RedstoneClockServiceFactory;
import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import net.onelitefeather.antiredstoneclockremastered.service.api.TranslationService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.*;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice module for core service dependencies.
 *
 * @author TheMeinerLP
 * @since 2.2.0
 * @version 1.0.0
 */
public final class ServiceModule extends AbstractModule {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceModule.class);

    @Override
    protected void configure() {
        bind(UpdateService.class).in(Singleton.class);
        bind(CheckTPS.class).in(Singleton.class);
    }
    
    @Provides
    @Singleton
    public RedstoneClockService provideRedstoneClockService(AntiRedstoneClockRemastered plugin,
                                                            RegionService regionService,
                                                            PlotsquaredSupport plotsquaredSupport,
                                                            WorldGuardSupport worldGuardSupport,
                                                            NotificationService notificationService) {
        return RedstoneClockServiceFactory.createService(plugin, regionService, plotsquaredSupport, worldGuardSupport,
                notificationService);
    }

    @Provides
    @Singleton
    public NotificationService providesNotificationService(AntiRedstoneClockRemastered antiRedstoneClockRemastered,
                                                           RegionService regionService) {
        var adminNotifications = new AdminNotificationService(antiRedstoneClockRemastered, null);
        var consoleNotification = new ConsoleNotificationService(antiRedstoneClockRemastered, adminNotifications);
        var signNotifications = new SignNotificationService(antiRedstoneClockRemastered, consoleNotification, regionService);
        return signNotifications;
    }

}