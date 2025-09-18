package net.onelitefeather.antiredstoneclockremastered.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.RedstoneClockService;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import javax.inject.Inject;

public final class ReloadCommand {

    private final RedstoneClockService redstoneClockService;

    @Inject
    public ReloadCommand(RedstoneClockService redstoneClockService) {
        this.redstoneClockService = redstoneClockService;
    }

    @Command("arcm reload")
    @CommandDescription("antiredstoneclockremastered.command.reload.description")
    @Permission("antiredstoneclockremastered.command.reload")
    public void reloadConfig(CommandSender commandSender) {
        this.redstoneClockService.reload();
        commandSender.sendMessage(Component.translatable("antiredstoneclockremastered.command.reload.success").arguments(AntiRedstoneClockRemastered.PREFIX));
    }
}
