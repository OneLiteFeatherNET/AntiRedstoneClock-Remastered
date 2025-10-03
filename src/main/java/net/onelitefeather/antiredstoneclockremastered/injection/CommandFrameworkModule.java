package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.onelitefeather.antiredstoneclockremastered.commands.DisplayActiveClocksCommand;
import net.onelitefeather.antiredstoneclockremastered.commands.FeatureCommand;
import net.onelitefeather.antiredstoneclockremastered.commands.ReloadCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

@Singleton
public class CommandFrameworkModule {
    private final JavaPlugin plugin;
    private final Injector injector;
    private AnnotationParser<CommandSender> annotationParser;

    @Inject
    public CommandFrameworkModule(JavaPlugin plugin, Injector injector) {
        this.plugin = plugin;
        this.injector = injector;
    }

    public void enable() {
        LegacyPaperCommandManager<CommandSender> commandManager = LegacyPaperCommandManager.createNative(
                plugin,
                ExecutionCoordinator.asyncCoordinator()
        );
        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier();
        } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
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
        registerCommands();
    }

    private void registerCommands() {
        if (this.annotationParser != null) {
            this.annotationParser.parse(injector.getInstance(ReloadCommand.class));
            this.annotationParser.parse(injector.getInstance(DisplayActiveClocksCommand.class));
            this.annotationParser.parse(injector.getInstance(FeatureCommand.class));
        }
    }
}

