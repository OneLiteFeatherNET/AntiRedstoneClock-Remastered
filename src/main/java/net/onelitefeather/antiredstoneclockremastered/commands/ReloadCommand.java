package net.onelitefeather.antiredstoneclockremastered.commands;

import jakarta.inject.Inject;
import net.kyori.adventure.text.Component;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.DecisionService;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command for reloading the plugin configuration.
 *
 * @author TheMeinerLP
 * @since 1.0.0
 * @version 1.0.0
 */
public final class ReloadCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReloadCommand.class);

    private final DecisionService decisionService;

    @Inject
    public ReloadCommand(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @Command("arcm reload")
    @CommandDescription("antiredstoneclockremastered.command.reload.description")
    @Permission("antiredstoneclockremastered.command.reload")
    public void reloadConfig(CommandSender commandSender) {
        LOGGER.debug("{} reloads the configuration", commandSender.getName());
        this.decisionService.reload();
        commandSender.sendMessage(Component.translatable("antiredstoneclockremastered.command.reload.success").arguments(AntiRedstoneClockRemastered.PREFIX));
    }
}
