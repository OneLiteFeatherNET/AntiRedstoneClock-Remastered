package net.onelitefeather.antiredstoneclockremastered.service.chain;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import org.jetbrains.annotations.NotNull;

public final class PlotSquaredRedstoneClockMiddleware extends RedstoneClockMiddleware {

    private final PlotsquaredSupport plotsquaredSupport;

    @Inject
    public PlotSquaredRedstoneClockMiddleware(PlotsquaredSupport plotsquaredSupport) {
        this.plotsquaredSupport = plotsquaredSupport;
    }

    @Override
    public @NotNull ResultState check(@NotNull CheckContext context) {
        if (this.plotsquaredSupport.isAllowedPlot(context.location())) return ResultState.SKIP;
        return checkNext(context);
    }
}
