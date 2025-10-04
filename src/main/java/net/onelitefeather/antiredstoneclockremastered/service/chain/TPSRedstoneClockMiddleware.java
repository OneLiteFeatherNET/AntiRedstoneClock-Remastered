package net.onelitefeather.antiredstoneclockremastered.service.chain;

import jakarta.inject.Inject;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.jetbrains.annotations.NotNull;

public final class TPSRedstoneClockMiddleware extends RedstoneClockMiddleware {

    private final CheckTPS checkTPS;

    @Inject
    public TPSRedstoneClockMiddleware(CheckTPS checkTPS) {
        this.checkTPS = checkTPS;
    }

    @Override
    public @NotNull ResultState check(@NotNull CheckContext context) {
        if (!this.checkTPS.isTpsOk()) return ResultState.SKIP;
        return checkNext(context);
    }
}
