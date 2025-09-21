package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.papermc.paper.ServerBuildInfo;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.api.WorldGuardSupport;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.service.factory.RedstoneClockServiceFactory;
import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import net.onelitefeather.antiredstoneclockremastered.service.api.TranslationService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.LegacyTranslationService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.ModernTranslationService;
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
    public RedstoneClockService provideRedstoneClockService(AntiRedstoneClockRemastered plugin, RegionService regionService,
                                                            PlotsquaredSupport plotsquaredSupport, WorldGuardSupport worldGuardSupport) {
        return RedstoneClockServiceFactory.createService(plugin, regionService, plotsquaredSupport, worldGuardSupport);
    }
    
    @Provides
    @Singleton
    public TranslationService provideTranslationService() {
        ServerBuildInfo buildInfo = ServerBuildInfo.buildInfo();
        if (buildInfo.minecraftVersionId().startsWith("1.20")) {
            LOGGER.info("Using legacy translation service");
            return new LegacyTranslationService();
        } else {
            LOGGER.info("Using modern translation service");
            return new ModernTranslationService();
        }
    }
}