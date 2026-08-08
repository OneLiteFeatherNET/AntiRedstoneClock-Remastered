package net.onelitefeather.antiredstoneclockremastered.service.tracking;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Detects hopper clocks by watching the direction of item movements between two hoppers.
 *
 * <p>A hopper clock is a pair of hoppers pointing at each other, so a single item travels
 * from hopper A to hopper B and back again. Item sorters and hopper chains only ever move
 * items in one direction, which is why only a <em>change</em> of direction within the same
 * hopper pair is reported as a clock cycle.</p>
 *
 * @author OneLiteFeather
 * @version 1.0.0
 * @since 2.9.0
 */
public final class HopperClockTracker {

    /**
     * Amount of registered movements after which expired pairs are dropped from memory.
     */
    private static final int PURGE_INTERVAL = 512;

    private final Map<HopperPair, Movement> movements = new ConcurrentHashMap<>();
    private final AtomicInteger movementsSincePurge = new AtomicInteger();

    /**
     * Registers an item movement between two hoppers.
     *
     * @param source          the hopper the item was taken from
     * @param destination     the hopper the item was moved into
     * @param nowSeconds      the current time in seconds
     * @param timeoutSeconds  how long a movement stays relevant for the opposite direction
     * @return {@code true} when this movement completed a back and forth cycle
     */
    public boolean registerMovement(@NotNull BlockKey source, @NotNull BlockKey destination,
                                    long nowSeconds, long timeoutSeconds) {
        if (source.equals(destination)) return false;

        if (this.movementsSincePurge.incrementAndGet() >= PURGE_INTERVAL) {
            this.movementsSincePurge.set(0);
            purgeExpired(nowSeconds, timeoutSeconds);
        }

        var pair = HopperPair.of(source, destination);
        var forward = pair.first().equals(source);
        var previous = this.movements.put(pair, new Movement(forward, nowSeconds));
        if (previous == null) return false;
        if (isExpired(previous, nowSeconds, timeoutSeconds)) return false;
        return previous.forward() != forward;
    }

    /**
     * Forgets the given hopper pair, e.g. after the clock has been handled.
     *
     * @param first  one hopper of the pair
     * @param second the other hopper of the pair
     */
    public void forget(@NotNull BlockKey first, @NotNull BlockKey second) {
        this.movements.remove(HopperPair.of(first, second));
    }

    /**
     * @return the amount of currently tracked hopper pairs
     */
    public int trackedPairs() {
        return this.movements.size();
    }

    private void purgeExpired(long nowSeconds, long timeoutSeconds) {
        this.movements.values().removeIf(movement -> isExpired(movement, nowSeconds, timeoutSeconds));
    }

    private boolean isExpired(@NotNull Movement movement, long nowSeconds, long timeoutSeconds) {
        return nowSeconds - movement.timestamp() > timeoutSeconds;
    }

    /**
     * Creates a key for the block at the given location.
     *
     * @param location the location of the hopper
     * @return the key or {@code null} when the location has no world
     */
    public static @Nullable BlockKey keyOf(@NotNull Location location) {
        var world = location.getWorld();
        if (world == null) return null;
        return new BlockKey(world.getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Identifies a single block without keeping a reference to the world or chunk.
     */
    public record BlockKey(@NotNull UUID world, int x, int y, int z) implements Comparable<BlockKey> {

        private static final Comparator<BlockKey> ORDER = Comparator.comparing(BlockKey::world)
                .thenComparingInt(BlockKey::x)
                .thenComparingInt(BlockKey::y)
                .thenComparingInt(BlockKey::z);

        @Override
        public int compareTo(@NotNull BlockKey other) {
            return ORDER.compare(this, other);
        }
    }

    /**
     * A pair of hoppers in a stable order, so both movement directions share one key.
     */
    public record HopperPair(@NotNull BlockKey first, @NotNull BlockKey second) {

        public static @NotNull HopperPair of(@NotNull BlockKey one, @NotNull BlockKey other) {
            return one.compareTo(other) <= 0 ? new HopperPair(one, other) : new HopperPair(other, one);
        }
    }

    /**
     * The last movement seen for a hopper pair.
     *
     * @param forward   whether the item moved from {@link HopperPair#first()} to {@link HopperPair#second()}
     * @param timestamp when the movement happened, in seconds
     */
    private record Movement(boolean forward, long timestamp) {
    }
}
