package net.onelitefeather.antiredstoneclockremastered;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.jeff_media.customblockdata.CustomBlockData;
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
import net.onelitefeather.antiredstoneclockremastered.service.logging.DebugLogging;
import net.onelitefeather.antiredstoneclockremastered.service.tracking.ConfigMode;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bukkit.plugin.java.JavaPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

public final class AntiRedstoneClockRemastered extends JavaPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntiRedstoneClockRemastered.class);

    /**
     * Directory inside the plugin folder the debug log files are written to.
     */
    private static final String DEBUG_LOG_DIRECTORY = "debug-logs";

    // Injector for dependency injection
    private Injector injector;

    public static final Component PREFIX = MiniMessage.miniMessage().deserialize("<gradient:red:white>[AntiRedstoneClock]</gradient>");

    @Override
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
        saveConfig();
        setupDebugLogging();
        LOGGER.debug("Creating injector with modules");
        injector = Guice.createInjector(Stage.PRODUCTION, Arrays.asList(
                new PlatformModule(this),
                new TranslationModule(),
                new ServiceModule(),
                new ExternalSupportModule(),
                new CommandModule(),
                new ListenerModule()
                )
        );
        LOGGER.debug("Injector created");
    }

    /**
     * Reloads the configuration and applies everything that depends on it.
     */
    public void reloadPluginConfig() {
        reloadConfig();
        setupDebugLogging();
        LOGGER.debug("Configuration reloaded");
    }

    /**
     * Opens or closes the debug log depending on the {@code debug.enabled} config option.
     *
     * <p>Debug messages are kept out of the server log on purpose, they can be very noisy.</p>
     */
    private void setupDebugLogging() {
        var enabled = getConfig().getBoolean("debug.enabled");
        if (enabled == DebugLogging.isEnabled()) return;

        if (!enabled) {
            LOGGER.debug("Debug logging has been disabled");
            DebugLogging.disable();
            return;
        }

        Path directory = getDataFolder().toPath().resolve(DEBUG_LOG_DIRECTORY);
        if (DebugLogging.enable(directory, getConfig().getInt("debug.keepFiles", 5))) {
            LOGGER.info("Debug logging is enabled, messages are written to {}",
                    directory.resolve(DebugLogging.LATEST_LOG_NAME));
            return;
        }
        LOGGER.warn("This server does not run on Log4j2, debug logging is not available");
    }

    @Override
    public void onEnable() {
        LOGGER.debug("Enabling plugin version {}", getPluginMeta().getVersion());
        CustomBlockData.registerListener(this);
        injector.getInstance(TranslationModule.class);
        var mode = ConfigMode.getEnum(getConfig(), "check.mode", ConfigMode.STATIC);
        getComponentLogger().info(mode.getEnableMessage());
        injector.getInstance(CheckTPS.class).startCheck();
        Optional.ofNullable(injector.getInstance(PlotsquaredSupport.class)).ifPresent(PlotsquaredSupport::init);
        donationInformation();
        injector.getInstance(UpdateService.class).schedule();
        injector.getInstance(UpdateService.class).run();
        injector.getInstance(UpdateService.class).notifyConsole(getComponentLogger());
        injector.getInstance(CommandFrameworkModule.class).enable();
        injector.getInstance(MetricsModule.class).registerCharts();
        this.injector.getInstance(ListenerModule.class).registerEvents(injector, this);
        LOGGER.debug("Plugin enabled in {} mode", mode);
    }

    @Override
    public void onDisable() {
        LOGGER.debug("Disabling plugin");
        injector.getInstance(UpdateService.class).shutdown();
        DebugLogging.disable();
    }

    private void donationInformation() {
        getComponentLogger().info(Component.translatable("antiredstoneclockremastered.notify.donation.console"));
    }

}
