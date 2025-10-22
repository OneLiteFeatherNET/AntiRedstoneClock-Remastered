package net.onelitefeather.antiredstoneclockremastered;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.injection.MetricsModule;
import net.onelitefeather.antiredstoneclockremastered.injection.CommandFrameworkModule;
import net.onelitefeather.antiredstoneclockremastered.injection.CommandModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ExternalSupportModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ListenerModule;
import net.onelitefeather.antiredstoneclockremastered.injection.PlatformModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ServiceModule;
import net.onelitefeather.antiredstoneclockremastered.injection.TranslationModule;
import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class AntiRedstoneClockRemastered extends JavaPlugin {
    
    // Injector for dependency injection
    private Injector injector;

    public static final Component PREFIX = MiniMessage.miniMessage().deserialize("<gradient:red:white>[AntiRedstoneClock]</gradient>");

    @Override
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
        saveConfig();
        injector = Guice.createInjector(Stage.PRODUCTION, Arrays.asList(
                new PlatformModule(this),
                new TranslationModule(),
                new ServiceModule(),
                new ExternalSupportModule(),
                new CommandModule(),
                new ListenerModule()
                )
        );
    }

    @Override
    public void onEnable() {
        injector.getInstance(TranslationModule.class);
        injector.getInstance(CheckTPS.class).startCheck();
        injector.getInstance(PlotsquaredSupport.class).init();
        donationInformation();
        injector.getInstance(UpdateService.class).schedule();
        injector.getInstance(UpdateService.class).run();
        injector.getInstance(UpdateService.class).notifyConsole(getComponentLogger());
        injector.getInstance(CommandFrameworkModule.class).enable();
        injector.getInstance(MetricsModule.class).registerCharts();
        this.injector.getInstance(ListenerModule.class).registerEvents(injector, this);
    }
    @Override
    public void onDisable() {
        injector.getInstance(UpdateService.class).shutdown();
    }

    private void donationInformation() {
        getComponentLogger().info(Component.translatable("antiredstoneclockremastered.notify.donation.console"));
    }

}
