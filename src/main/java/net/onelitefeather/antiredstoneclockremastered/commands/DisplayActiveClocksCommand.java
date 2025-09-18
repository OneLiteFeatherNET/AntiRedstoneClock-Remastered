package net.onelitefeather.antiredstoneclockremastered.commands;

import jakarta.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.feature.pagination.Pagination;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Command for displaying active redstone clocks.
 *
 * @author TheMeinerLP
 * @since 1.0.0
 * @version 1.0.0
 */
public final class DisplayActiveClocksCommand implements Pagination.Renderer.RowRenderer<RedstoneClock> {

    private final RedstoneClockService redstoneClockService;

    private final Pagination.Builder pagination = Pagination.builder().resultsPerPage(4);

    @Inject
    public DisplayActiveClocksCommand(RedstoneClockService redstoneClockService) {
        this.redstoneClockService = redstoneClockService;
    }

    @Command("arcm display [page]")
    @CommandDescription("antiredstoneclockremastered.command.display.description")
    @Permission("antiredstoneclockremastered.command.display")
    public void displayClocks(CommandSender commandSender, @Greedy @Argument("page") Integer page) {
        Pagination<RedstoneClock> build = pagination.build(
                AntiRedstoneClockRemastered.PREFIX,
                this,
                this::mapToCommand);
        if (page == null) {
            page = 0;
        }
        build.render(this.redstoneClockService.getRedstoneClocks(), Math.max(1, page))
                .forEach(commandSender::sendMessage);

    }

    private String mapToCommand(int i) {
        return "/arcm display " + i;
    }

    private Component mapClockToMessage(RedstoneClock redstoneClock) {
        var location = redstoneClock.getLocation();
        return Component.empty().hoverEvent(Component.translatable("antiredstoneclockremastered.command.display.clock.hover").asHoverEvent()).append(
                Component.translatable("antiredstoneclockremastered.command.display.clock.text")
                        .arguments(
                                TranslationArgument.numeric(redstoneClock.getTriggerCount()),
                                TranslationArgument.numeric(location.getBlockX()),
                                TranslationArgument.numeric(location.getBlockY()),
                                TranslationArgument.numeric(location.getBlockZ()),
                                Component.empty()
                                        .clickEvent(ClickEvent.callback(audience -> {
                                            if (audience instanceof final Player executor) {
                                                executor.teleport(location);
                                            }
                                        }))
                        )
        );
    }

    @Override
    public @NotNull Collection<Component> renderRow(@Nullable RedstoneClock redstoneClock, int index) {
        return List.of(mapClockToMessage(redstoneClock));
    }
}
