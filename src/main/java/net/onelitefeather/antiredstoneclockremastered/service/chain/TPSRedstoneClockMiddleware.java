package net.onelitefeather.antiredstoneclockremastered.service.chain;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

public final class TPSRedstoneClockMiddleware extends RedstoneClockMiddleware {

    private static final Logger LOGGER = LoggerFactory.getLogger(TPSRedstoneClockMiddleware.class);

    private final CheckTPS checkTPS;

    @Inject
    public TPSRedstoneClockMiddleware(CheckTPS checkTPS) {
        this.checkTPS = checkTPS;
    }

    @Override
    public @NotNull ResultState check(@NotNull CheckContext context) {
        if (!this.checkTPS.isTpsOk()) {
            LOGGER.debug("Skipping {} check, the configured TPS range is not met (current: {})",
                    context.eventType(), this.checkTPS.getTps());
            return ResultState.SKIP;
        }
        return checkNext(context);
    }
}
