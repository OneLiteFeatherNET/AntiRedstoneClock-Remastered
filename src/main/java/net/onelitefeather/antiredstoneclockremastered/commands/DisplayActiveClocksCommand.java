package net.onelitefeather.antiredstoneclockremastered.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.ClickEvent;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

public final class DisplayActiveClocksCommand {

    private final AntiRedstoneClockRemastered plugin;

    public DisplayActiveClocksCommand(AntiRedstoneClockRemastered plugin) {
        this.plugin = plugin;
    }

    @Command("arcm display")
    @CommandDescription("antiredstoneclockremastered.command.display.description")
    @Permission("antiredstoneclockremastered.command.display")
    public void displayClocks(CommandSender commandSender) {
        commandSender.sendMessage(Component.translatable("antiredstoneclockremastered.command.display.clock.title").arguments(AntiRedstoneClockRemastered.PREFIX));
        this.plugin.getRedstoneClockService().getRedstoneClocks().stream().map(this::mapClockToMessage).forEach(commandSender::sendMessage);

    }

    private Component mapClockToMessage(RedstoneClock redstoneClock) {
        var location = redstoneClock.getLocation();
        return Component.translatable("antiredstoneclockremastered.command.display.clock.text")
                .arguments(
                        TranslationArgument.numeric(location.getBlockX()),
                        TranslationArgument.numeric(location.getBlockY()),
                        TranslationArgument.numeric(location.getBlockZ()),
                        TranslationArgument.numeric(redstoneClock.getTriggerCount()),
                        Component.empty()
                                .hoverEvent(Component.translatable("antiredstoneclockremastered.command.display.clock.hover").asHoverEvent())
                                .clickEvent(ClickEvent.callback(audience -> {
                                    if (audience instanceof final Player executor) {
                                        executor.teleport(location);
                                    }
                                }))
                );
    }
}
