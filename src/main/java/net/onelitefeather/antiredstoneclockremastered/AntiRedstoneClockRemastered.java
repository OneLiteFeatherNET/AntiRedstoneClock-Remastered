package net.onelitefeather.antiredstoneclockremastered;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.api.WorldGuardSupport;
import net.onelitefeather.antiredstoneclockremastered.commands.DisplayActiveClocksCommand;
import net.onelitefeather.antiredstoneclockremastered.commands.FeatureCommand;
import net.onelitefeather.antiredstoneclockremastered.commands.ReloadCommand;
import net.onelitefeather.antiredstoneclockremastered.injection.CommandModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ExternalSupportModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ListenerModule;
import net.onelitefeather.antiredstoneclockremastered.injection.PlatformModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ServiceModule;
import net.onelitefeather.antiredstoneclockremastered.injection.TranslationModule;
import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

public final class AntiRedstoneClockRemastered extends JavaPlugin {
    
    // Injector for dependency injection
    private Injector injector;
    
    // Legacy dependencies that are still manually managed
    private CheckTPS tps;
    private Metrics metrics;
    private AnnotationParser<CommandSender> annotationParser;

    public static final Component PREFIX = MiniMessage.miniMessage().deserialize("<gradient:red:white>[AntiRedstoneClock]</gradient>");

    @Override
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
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
        this.tps = injector.getInstance(CheckTPS.class);

        // Initialize other components
        donationInformation();
        updateService();
        enableCommandFramework();
        enableTPSChecker();
        enableBStatsSupport();
        this.injector.getInstance(ListenerModule.class).registerEvents(injector, this);
        registerCommands();
    }

    private void updateService() {
        injector.getInstance(UpdateService.class).schedule();
        injector.getInstance(UpdateService.class).run();
        injector.getInstance(UpdateService.class).notifyConsole(getComponentLogger());
    }

    @Override
    public void onDisable() {
        injector.getInstance(UpdateService.class).shutdown();
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
        return this.injector.getInstance(PlotsquaredSupport.class).getVersion();
    }

    private String bstatsWorldGuardVersion() {
        return this.injector.getInstance(WorldGuardSupport.class).getVersion();
    }

}
