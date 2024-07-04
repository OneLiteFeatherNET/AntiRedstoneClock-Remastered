package net.onelitefeather.antiredstoneclockremastered.plotsquared.v7;

import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class RedstoneClockFlag extends BooleanFlag<RedstoneClockFlag> {

    public static final RedstoneClockFlag REDSTONE_CLOCK_TRUE = new RedstoneClockFlag(true);
    public static final RedstoneClockFlag REDSTONE_CLOCK_FALSE = new RedstoneClockFlag(false);

    protected RedstoneClockFlag(boolean value) {
        super(value, StaticCaption.of("Set to `false` to disable RedstoneClock in the plot."));
    }

    @Override
    protected RedstoneClockFlag flagOf(@NonNull Boolean value) {
        return value ? REDSTONE_CLOCK_TRUE : REDSTONE_CLOCK_FALSE;
    }
}
