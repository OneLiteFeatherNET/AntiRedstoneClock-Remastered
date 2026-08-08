package net.onelitefeather.antiredstoneclockremastered.service.chain;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

public final class PlotSquaredRedstoneClockMiddleware extends RedstoneClockMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlotSquaredRedstoneClockMiddleware.class);

    private final PlotsquaredSupport plotsquaredSupport;

    @Inject
    public PlotSquaredRedstoneClockMiddleware(PlotsquaredSupport plotsquaredSupport) {
        this.plotsquaredSupport = plotsquaredSupport;
    }

    @Override
    public @NotNull ResultState check(@NotNull CheckContext context) {
        if (this.plotsquaredSupport.isAllowedPlot(context.location())) {
            LOGGER.debug("Skipping {} check, a PlotSquared plot allows redstone clocks at {}",
                    context.eventType(), context.location());
            return ResultState.SKIP;
        }
        return checkNext(context);
    }
}
