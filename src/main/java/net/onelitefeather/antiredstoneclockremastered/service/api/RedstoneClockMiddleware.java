package net.onelitefeather.antiredstoneclockremastered.service.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public abstract class RedstoneClockMiddleware {

    private RedstoneClockMiddleware next;

    public static RedstoneClockMiddleware link(RedstoneClockMiddleware first, RedstoneClockMiddleware... chain) {
        var head = first;
        for (var nextInChain : chain) {
            head.next = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    public abstract ResultState check(CheckContext context);

    protected ResultState checkNext(CheckContext checkContext) {
        return this.next.check(checkContext);
    }

    public enum ResultState {
        REMOVE_AND_DROP,
        REMOVE_AND_WITHOUT_DROP,
        ONLY_NOTIFY,
        SKIP
    }

    public enum EventType {
        PISTON,
        REDSTONE_AND_REPEATER,
        SCULK_SENSOR,
        COMPARATOR,
        OBSERVER,
    }

    public record CheckContext(Location location, Block block, @Nullable Boolean state, EventType eventType) {
        public static CheckContext of(Location location, Block block, @Nullable Boolean state, EventType eventType) {
            return new CheckContext(location, block, state, eventType);
        }

        public static CheckContext of(Block block, @Nullable Boolean state, EventType eventType) {
            return new CheckContext(block.getLocation(), block, state, eventType);
        }

        public static CheckContext of(Block block, EventType eventType) {
            return new CheckContext(block.getLocation(), block, null, eventType);
        }
    }
}
