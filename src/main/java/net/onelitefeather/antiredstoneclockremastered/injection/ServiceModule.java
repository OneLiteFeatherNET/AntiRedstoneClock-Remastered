package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import jakarta.inject.Named;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.*;
import net.onelitefeather.antiredstoneclockremastered.service.chain.*;
import net.onelitefeather.antiredstoneclockremastered.service.factory.DecisionServiceFactory;
import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import net.onelitefeather.antiredstoneclockremastered.service.notification.AdminNotificationService;
import net.onelitefeather.antiredstoneclockremastered.service.notification.ConsoleNotificationService;
import net.onelitefeather.antiredstoneclockremastered.service.notification.DiscordNotificationService;
import net.onelitefeather.antiredstoneclockremastered.service.notification.SignNotificationService;
import net.onelitefeather.antiredstoneclockremastered.service.tracking.DelegatedTrackingService;
import net.onelitefeather.antiredstoneclockremastered.service.tracking.StaticTrackingService;
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
    public NotificationService providesNotificationService(AntiRedstoneClockRemastered antiRedstoneClockRemastered,
                                                           RegionService regionService) {
        var adminNotifications = new AdminNotificationService(antiRedstoneClockRemastered, null);
        var consoleNotification = new ConsoleNotificationService(antiRedstoneClockRemastered, adminNotifications);
        var signNotifications = new SignNotificationService(antiRedstoneClockRemastered, consoleNotification, regionService);
        var discordNotification = new DiscordNotificationService(antiRedstoneClockRemastered, signNotifications);
        return discordNotification;
    }

    @Provides
    @Named("staticTrackingService")
    @Singleton
    public RedstoneTrackingService providesStaticTrackingService(Injector injector) {
        return injector.getInstance(StaticTrackingService.class);
    }

    @Provides
    @Singleton
    public RedstoneTrackingService providesDelegatedTrackingService(Injector injector) {
        return injector.getInstance(DelegatedTrackingService.class);
    }

    @Provides
    @Singleton
    public RedstoneClockMiddleware provideRedstoneClockMiddleware(Injector injector) {
        return RedstoneClockMiddleware.link(
                injector.getInstance(TPSRedstoneClockMiddleware.class),
                injector.getInstance(SkipEventTypeRedstoneClockMiddleware.class),
                injector.getInstance(WorldRedstoneClockMiddleware.class),
                injector.getInstance(WorldGuardRedstoneClockMiddleware.class),
                injector.getInstance(PlotSquaredRedstoneClockMiddleware.class),
                injector.getInstance(TrackingRedstoneClockMiddleware.class)
                );
    }

    @Provides
    @Singleton
    public DecisionService provideDecisionService(AntiRedstoneClockRemastered plugin,
                                                  RegionService regionService,
                                                  RedstoneClockMiddleware middleware,
                                                  NotificationService notificationService) {
        return DecisionServiceFactory.createService(plugin, regionService, middleware, notificationService);
    }

}