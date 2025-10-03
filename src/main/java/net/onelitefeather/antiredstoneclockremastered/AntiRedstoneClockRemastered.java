package net.onelitefeather.antiredstoneclockremastered;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.api.WorldGuardSupport;
import net.onelitefeather.antiredstoneclockremastered.commands.DisplayActiveClocksCommand;
import net.onelitefeather.antiredstoneclockremastered.commands.FeatureCommand;
import net.onelitefeather.antiredstoneclockremastered.commands.ReloadCommand;
import net.onelitefeather.antiredstoneclockremastered.injection.CommandModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ExternalSupportModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ListenerModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ServiceModule;
import net.onelitefeather.antiredstoneclockremastered.listener.*;
import net.onelitefeather.antiredstoneclockremastered.plotsquared.v6.PlotSquaredLegacySupport;
import net.onelitefeather.antiredstoneclockremastered.plotsquared.v7.PlotSquaredModernSupport;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.factory.RedstoneClockServiceFactory;
import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import net.onelitefeather.antiredstoneclockremastered.service.api.TranslationService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.LegacyTranslationService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.ModernTranslationService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import net.onelitefeather.antiredstoneclockremastered.worldguard.v6.WorldGuardLegacySupport;
import net.onelitefeather.antiredstoneclockremastered.worldguard.v7.WorldGuardModernSupport;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

public final class AntiRedstoneClockRemastered extends JavaPlugin {
    public static final String RESOURCE_BUNDLE_NAME = "antiredstoneclockremasterd";
    
    // Injector for dependency injection
    private Injector injector;
    
    // Legacy dependencies that are still manually managed
    private CheckTPS tps;
    private RedstoneClockService redstoneClockService;
    private WorldGuardSupport worldGuardSupport;
    private PlotsquaredSupport plotsquaredSupport;
    private Metrics metrics;
    private AnnotationParser<CommandSender> annotationParser;
    private UpdateService updateService;

    public static final Component PREFIX = MiniMessage.miniMessage().deserialize("<gradient:red:white>[AntiRedstoneClock]</gradient>");

    @Override
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
        saveConfig();
        injector = Guice.createInjector(
            new ServiceModule(this),
            new ExternalSupportModule(this),
            new CommandModule(),
            new ListenerModule()
        );
        this.worldGuardSupport = injector.getInstance(WorldGuardSupport.class);
    }

    @Override
    public void onEnable() {
        TranslationService translationService = injector.getInstance(TranslationService.class);
        this.redstoneClockService = injector.getInstance(RedstoneClockService.class);
        this.updateService = injector.getInstance(UpdateService.class);
        this.tps = injector.getInstance(CheckTPS.class);
        this.plotsquaredSupport = injector.getInstance(PlotsquaredSupport.class);
        
        // Setup translations
        Path langFolder = getDataFolder().toPath().resolve("lang");
        if (Files.notExists(langFolder)) {
            try {
                Files.createDirectories(langFolder);
            } catch (IOException e) {
                getSLF4JLogger().error("An error occurred while creating lang folder");
                return;
            }
        }
        var languages = new HashSet<>(getConfig().getStringList("translations"));
        languages.add("en-US");
        languages.stream()
            .map(Locale::forLanguageTag)
            .forEach(locale -> loadAndRegisterTranslation(locale, langFolder, translationService));
        translationService.registerGlobal();
        
        // Initialize other components
        donationInformation();
        updateService();
        enableCommandFramework();
        enableTPSChecker();
        enableBStatsSupport();
        registerEvents();
        registerCommands();
    }

    private void updateService() {
        this.updateService.run();
        this.updateService.notifyConsole(getComponentLogger());
    }

    @Override
    public void onDisable() {
        if (this.updateService != null) {
            this.updateService.shutdown();
        }
    }

    private void donationInformation() {
        getComponentLogger().info(Component.translatable("antiredstoneclockremastered.notify.donation.console"));
    }

    private void registerCommands() {
        if (this.annotationParser != null) {
            this.annotationParser.parse(injector.getInstance(ReloadCommand.class));
            this.annotationParser.parse(injector.getInstance(DisplayActiveClocksCommand.class));
            this.annotationParser.parse(injector.getInstance(FeatureCommand.class));
        }
    }

    private void enableCommandFramework() {
        LegacyPaperCommandManager<CommandSender> commandManager = LegacyPaperCommandManager.createNative(
                this,
                ExecutionCoordinator.asyncCoordinator()
        );
        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier();
        }
        else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            commandManager.registerAsynchronousCompletions();
        }
        annotationParser = new AnnotationParser<>(commandManager, CommandSender.class);
        annotationParser.descriptionMapper(string -> RichDescription.of(Component.translatable(string)));
        MinecraftHelp<CommandSender> help = MinecraftHelp.create(
                "/arcm help",
                commandManager,
                sender -> sender
        );
        commandManager.command(
                commandManager.commandBuilder("arcm").literal("help")
                        .permission("antiredstoneclockremastered.command.help")
                        .optional("query", greedyStringParser(), DefaultValue.constant(""))
                        .handler(context -> {
                            help.queryCommands(context.get("query"), context.sender());
                        })
        );
    }

    // External support now handled by DI in ExternalSupportModule

    private void registerEvents() {
        // Register DI-enabled listeners
        getServer().getPluginManager().registerEvents(injector.getInstance(PlayerListener.class), this);
        
        if (getConfig().getBoolean("check.observer", true)) {
            getServer().getPluginManager().registerEvents(injector.getInstance(ObserverListener.class), this);
        }
        
        if (getConfig().getBoolean("check.sculk", true)) {
            var sculk = Material.getMaterial("SCULK");
            if (sculk != null) {
                getServer().getPluginManager().registerEvents(injector.getInstance(SculkListener.class), this);
            }
        }
        
        if (getConfig().getBoolean("check.piston", true)) {
            getServer().getPluginManager().registerEvents(injector.getInstance(PistonListener.class), this);
        }
        
        // Material-dependent listeners now use dependency injection
        if (getConfig().getBoolean("check.comparator", true)) {
            var comparator = Material.getMaterial("COMPARATOR");
            if (comparator != null) {
                var listener = injector.getInstance(ListenerModule.class)
                    .createComparatorListener(comparator, redstoneClockService, tps, this);
                getServer().getPluginManager().registerEvents(listener, this);
            } else {
                var listener1 = injector.getInstance(ListenerModule.class)
                    .createComparatorListener(Material.getMaterial("REDSTONE_COMPARATOR_OFF"), redstoneClockService, tps, this);
                var listener2 = injector.getInstance(ListenerModule.class)
                    .createComparatorListener(Material.getMaterial("REDSTONE_COMPARATOR_ON"), redstoneClockService, tps, this);
                getServer().getPluginManager().registerEvents(listener1, this);
                getServer().getPluginManager().registerEvents(listener2, this);
            }
        }
        
        if (getConfig().getBoolean("check.redstoneAndRepeater", true)) {
            var repeater = Material.getMaterial("REPEATER");
            if (repeater != null) {
                var listener = injector.getInstance(ListenerModule.class)
                    .createRedstoneListener(repeater, redstoneClockService, tps, this);
                getServer().getPluginManager().registerEvents(listener, this);
            } else {
                var listener1 = injector.getInstance(ListenerModule.class)
                    .createRedstoneListener(Material.getMaterial("DIODE_BLOCK_ON"), redstoneClockService, tps, this);
                var listener2 = injector.getInstance(ListenerModule.class)
                    .createRedstoneListener(Material.getMaterial("DIODE_BLOCK_OFF"), redstoneClockService, tps, this);
                getServer().getPluginManager().registerEvents(listener1, this);
                getServer().getPluginManager().registerEvents(listener2, this);
            }
        }
    }

    private void enableTPSChecker() {
        this.tps.startCheck();
    }

    private void enableBStatsSupport() {
        this.metrics = new Metrics(this, 19085);
        this.metrics.addCustomChart(new SimplePie("worldguard", this::bstatsWorldGuardVersion));
        this.metrics.addCustomChart(new SimplePie("plotsquared", this::bstatsPlotSquaredVersion));
        this.metrics.addCustomChart(new DrilldownPie("maxcount", this::bstatsMaxCount));
    }

    private Map<String, Map<String, Integer>> bstatsMaxCount() {
        var map = new HashMap<String, Map<String, Integer>>();
        var count = getConfig().getInt("clock.maxCount");
        var entry = Map.of(String.valueOf(count), 1);
        switch (count) {
            case 0 -> map.put("0 \uD83D\uDEAB", entry);
            case 1, 2, 3, 4, 5 -> map.put("1-5 \uD83D\uDE10", entry);
            case 6, 7, 8, 9, 10 -> map.put("6-10 \uD83D\uDE42", entry);
            case 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 -> map.put("11-25 \uD83D\uDE0A", entry);
            case 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50 ->
                    map.put("26-50 \uD83D\uDE00", entry);
            default -> map.put("50+ \uD83D\uDE01", entry);
        }
        return map;
    }

    private String bstatsPlotSquaredVersion() {
        if (this.plotsquaredSupport != null) {
            return this.plotsquaredSupport.getVersion();
        }
        return "unknown";
    }

    private String bstatsWorldGuardVersion() {
        if (this.worldGuardSupport != null) {
            return this.worldGuardSupport.getVersion();
        }
        return "unknown";
    }

    private void loadAndRegisterTranslation(Locale locale, Path langFolder, TranslationService translationService) {
        try {
            ResourceBundle bundle = loadResourceBundle(locale, langFolder);
            if (bundle != null) {
                translationService.registerAll(locale, bundle, false);
            }
        } catch (Exception e) {
            getSLF4JLogger().error("An error occurred while loading language file for locale {}", locale, e);
        }
    }

    private ResourceBundle loadResourceBundle(Locale locale, Path langFolder) throws Exception {
        Path langFile = langFolder.resolve(RESOURCE_BUNDLE_NAME + "_" + locale.toLanguageTag() + ".properties");

        if (Files.exists(langFile)) {
            try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{langFolder.toUri().toURL()})) {
                return ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale, urlClassLoader, UTF8ResourceBundleControl.get());
            }
        } else {
            return ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale, UTF8ResourceBundleControl.get());
        }
    }

    public CheckTPS getTps() {
        return tps;
    }

    public RedstoneClockService getRedstoneClockService() {
        return redstoneClockService;
    }

    public WorldGuardSupport getWorldGuardSupport() {
        return worldGuardSupport;
    }

    public PlotsquaredSupport getPlotsquaredSupport() {
        return plotsquaredSupport;
    }

    public UpdateService getUpdateService() {
        return updateService;
    }
}
