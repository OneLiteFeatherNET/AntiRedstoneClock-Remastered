package net.onelitefeather.antiredstoneclockremastered.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

public final class ReloadCommand {

    private final AntiRedstoneClockRemastered plugin;

    public ReloadCommand(AntiRedstoneClockRemastered plugin) {
        this.plugin = plugin;
    }

    @Command("arcm reload")
    @CommandDescription("antiredstoneclockremastered.command.reload.description")
    @Permission("antiredstoneclockremastered.command.reload")
    public void reloadConfig(CommandSender commandSender) {
        this.plugin.getRedstoneClockService().reload();
        plugin.adventure().sender(commandSender).sendMessage(Component.translatable("antiredstoneclockremastered.command.reload.success").arguments(AntiRedstoneClockRemastered.PREFIX));

    }
}
