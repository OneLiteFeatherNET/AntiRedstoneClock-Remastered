package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.papermc.paper.ServerBuildInfo;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import net.onelitefeather.antiredstoneclockremastered.service.api.TranslationService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.LegacyTranslationService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.ModernTranslationService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;

/**
 * Guice module for core service dependencies
 */
public class ServiceModule extends AbstractModule {
    
    private final AntiRedstoneClockRemastered plugin;
    
    public ServiceModule(AntiRedstoneClockRemastered plugin) {
        this.plugin = plugin;
    }
    
    @Override
    protected void configure() {
        bind(AntiRedstoneClockRemastered.class).toInstance(plugin);
        bind(RedstoneClockService.class).in(Singleton.class);
        bind(UpdateService.class).in(Singleton.class);
        bind(CheckTPS.class).in(Singleton.class);
    }
    
    @Provides
    @Singleton
    public TranslationService provideTranslationService() {
        ServerBuildInfo buildInfo = ServerBuildInfo.buildInfo();
        if (buildInfo.minecraftVersionId().startsWith("1.20")) {
            plugin.getSLF4JLogger().info("Using legacy translation service");
            return new LegacyTranslationService();
        } else {
            plugin.getSLF4JLogger().info("Using modern translation service");
            return new ModernTranslationService();
        }
    }
}