package net.onelitefeather.antiredstoneclockremastered.commands;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.feature.pagination.Pagination;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;
import org.bukkit.command.CommandSender;
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

    private final RedstoneTrackingService trackingService;

    private final Pagination.Builder pagination = Pagination.builder().resultsPerPage(4);

    @Inject
    public DisplayActiveClocksCommand(RedstoneTrackingService trackingService) {
        this.trackingService = trackingService;
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
        build.render(this.trackingService.getRedstoneClocks(), Math.max(1, page))
                .forEach(commandSender::sendMessage);

    }

    private String mapToCommand(int i) {
        return "/arcm display " + i;
    }

    @Override
    public @NotNull Collection<Component> renderRow(@Nullable RedstoneClock redstoneClock, int index) {
        if (redstoneClock == null) return List.of();
        return List.of(redstoneClock.render());
    }
}
