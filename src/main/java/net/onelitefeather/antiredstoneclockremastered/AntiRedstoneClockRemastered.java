package net.onelitefeather.antiredstoneclockremastered;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.api.WorldGuardSupport;
import net.onelitefeather.antiredstoneclockremastered.commands.DisplayActiveClocksCommand;
import net.onelitefeather.antiredstoneclockremastered.commands.FeatureCommand;
import net.onelitefeather.antiredstoneclockremastered.commands.ReloadCommand;
import net.onelitefeather.antiredstoneclockremastered.listener.ComparatorListener;
import net.onelitefeather.antiredstoneclockremastered.listener.ObserverListener;
import net.onelitefeather.antiredstoneclockremastered.listener.PistonListener;
import net.onelitefeather.antiredstoneclockremastered.listener.PlayerListener;
import net.onelitefeather.antiredstoneclockremastered.listener.RedstoneListener;
import net.onelitefeather.antiredstoneclockremastered.listener.SculkListener;
import net.onelitefeather.antiredstoneclockremastered.plotsquared.v6.PlotSquaredLegacySupport;
import net.onelitefeather.antiredstoneclockremastered.plotsquared.v7.PlotSquaredModernSupport;
import net.onelitefeather.antiredstoneclockremastered.service.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.translations.PluginTranslationRegistry;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import net.onelitefeather.antiredstoneclockremastered.worldguard.v6.WorldGuardLegacySupport;
import net.onelitefeather.antiredstoneclockremastered.worldguard.v7.WorldGuardModernSupport;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
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
import org.incendo.cloud.paper.PaperCommandManager;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

public final class AntiRedstoneClockRemastered extends JavaPlugin {
    private CheckTPS tps;

    private RedstoneClockService redstoneClockService;
    private WorldGuardSupport worldGuardSupport;

    private PlotsquaredSupport plotsquaredSupport;

    private Metrics metrics;
    private AnnotationParser<CommandSender> annotationParser;

    public static final Component PREFIX = MiniMessage.miniMessage().deserialize("<gradient:red:white>[AntiRedstoneClock]</gradient>");

    @Override
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
        enableWorldGuardSupport();
    }


    @Override
    public void onEnable() {
        final TranslationRegistry translationRegistry = new PluginTranslationRegistry(TranslationRegistry.create(Key.key("antiredstoneclockremastered", "translations")));
        translationRegistry.defaultLocale(Locale.US);
        Path langFolder = getDataFolder().toPath().resolve("lang");
        if (Files.exists(langFolder)) {
            try (var urlClassLoader = new URLClassLoader(new URL[]{langFolder.toUri().toURL()})) {
                getConfig().getStringList("translations").stream().map(Locale::forLanguageTag).forEach(r -> {
                    var bundle = ResourceBundle.getBundle("antiredstoneclockremasterd", r, urlClassLoader, UTF8ResourceBundleControl.get());
                    translationRegistry.registerAll(r, bundle, false);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            getConfig().getStringList("translations").stream().map(Locale::forLanguageTag).forEach(r -> {
                var bundle = ResourceBundle.getBundle("antiredstoneclockremasterd", r, UTF8ResourceBundleControl.get());
                translationRegistry.registerAll(r, bundle, false);
            });
        }
        GlobalTranslator.translator().addSource(translationRegistry);
        enableCommandFramework();
        enablePlotsquaredSupport();
        enableTPSChecker();
        enableRedstoneClockService();
        enableBStatsSupport();
        registerEvents();
        registerCommands();
    }

    @Override
    public void onDisable() {
    }

    private void registerCommands() {
        if (this.annotationParser != null) {
            this.annotationParser.parse(new ReloadCommand(this));
            this.annotationParser.parse(new DisplayActiveClocksCommand(this));
            this.annotationParser.parse(new FeatureCommand(this));
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

    private void enablePlotsquaredSupport() {
        Plugin plugin = getServer().getPluginManager().getPlugin("PlotSquared");
        if (plugin == null) {
            this.getLogger().warning("PlotSquared hasn't been found!");
            return;
        }
        @SuppressWarnings("deprecation")
        int psVersion = Integer.parseInt(plugin.getDescription().getVersion().split("\\.")[0]);
        if (psVersion < 6) {
            getLogger().warning("We don't support PS5 currently also you use a unsupported version of PlotSquared!!!");
            return;
        } else if (psVersion < 7) {
            getLogger().warning("You use a legacy version of PlotSquared!");
            this.plotsquaredSupport = new PlotSquaredLegacySupport();
        } else {
            getLogger().info("Thanks to hold your software up-to date <3");
            this.plotsquaredSupport = new PlotSquaredModernSupport();
        }
        this.plotsquaredSupport.init();
    }

    private void enableWorldGuardSupport() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null) {
            this.getLogger().warning("WorldGuard hasn't been found!");
            return;
        }
        @SuppressWarnings("deprecation")
        int wgVersion = Integer.parseInt(plugin.getDescription().getVersion().split("\\.")[0]);
        if (wgVersion > 6) {
            this.worldGuardSupport = new WorldGuardModernSupport(this);
        } else {
            this.worldGuardSupport = new WorldGuardLegacySupport(this);
        }

        if (this.worldGuardSupport.registerFlag()) {
            this.getLogger().info("Flag redstoneclock registered");
        } else {
            this.getLogger().severe("An error occurred while registering redstoneclock flag");
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this.redstoneClockService), this);
        if (getConfig().getBoolean("check.observer", true)) {
            getServer().getPluginManager().registerEvents(new ObserverListener(this), this);
        }
        if (getConfig().getBoolean("check.sculk", true)) {
            var sculk = Material.getMaterial("SCULK");
            if (sculk != null) {
                getServer().getPluginManager().registerEvents(new SculkListener(this), this);
            }
        }
        if (getConfig().getBoolean("check.piston", true)) {
            getServer().getPluginManager().registerEvents(new PistonListener(this), this);
        }
        if (getConfig().getBoolean("check.comparator", true)) {
            var comparator = Material.getMaterial("COMPARATOR");
            if (comparator != null) {
                getServer().getPluginManager().registerEvents(new ComparatorListener(comparator, this), this);
            } else {
                getServer().getPluginManager().registerEvents(new ComparatorListener(Material.getMaterial("REDSTONE_COMPARATOR_OFF"), this), this);
                getServer().getPluginManager().registerEvents(new ComparatorListener(Material.getMaterial("REDSTONE_COMPARATOR_ON"), this), this);
            }
        }
        if (getConfig().getBoolean("check.redstoneAndRepeater", true)) {
            var repeater = Material.getMaterial("REPEATER");
            if (repeater != null) {
                getServer().getPluginManager().registerEvents(new RedstoneListener(repeater, this), this);
            } else {
                getServer().getPluginManager().registerEvents(new RedstoneListener(Material.getMaterial("DIODE_BLOCK_ON"), this), this);
                getServer().getPluginManager().registerEvents(new RedstoneListener(Material.getMaterial("DIODE_BLOCK_OFF"), this), this);
            }
        }
    }

    private void enableRedstoneClockService() {
        this.redstoneClockService = new RedstoneClockService(this);
    }

    private void enableTPSChecker() {
        this.tps = new CheckTPS(this,
                getConfig().getInt("tps.interval", 2),
                getConfig().getInt("tps.max", 20),
                getConfig().getInt("tps.min", 15)
        );
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


}
