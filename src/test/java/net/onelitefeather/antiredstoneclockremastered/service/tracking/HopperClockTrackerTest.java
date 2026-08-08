package net.onelitefeather.antiredstoneclockremastered.service.tracking;

import net.onelitefeather.antiredstoneclockremastered.service.tracking.HopperClockTracker.BlockKey;
import net.onelitefeather.antiredstoneclockremastered.service.tracking.HopperClockTracker.HopperPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the hopper clock detection.
 */
class HopperClockTrackerTest {

    private static final UUID WORLD = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_WORLD = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final long TIMEOUT = 300L;

    private static final BlockKey HOPPER_A = new BlockKey(WORLD, 10, 64, 10);
    private static final BlockKey HOPPER_B = new BlockKey(WORLD, 11, 64, 10);
    private static final BlockKey HOPPER_C = new BlockKey(WORLD, 12, 64, 10);

    private HopperClockTracker tracker;

    @BeforeEach
    void setUp() {
        this.tracker = new HopperClockTracker();
    }

    @Test
    @DisplayName("A single movement is not a clock")
    void singleMovementIsNoClock() {
        assertThat(this.tracker.registerMovement(HOPPER_A, HOPPER_B, 0, TIMEOUT)).isFalse();
    }

    @Test
    @DisplayName("Movements in the same direction are not a clock")
    void sameDirectionIsNoClock() {
        for (int second = 0; second < 50; second++) {
            assertThat(this.tracker.registerMovement(HOPPER_A, HOPPER_B, second, TIMEOUT))
                    .as("movement %s", second)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("A hopper chain of a sorting system is not a clock")
    void hopperChainIsNoClock() {
        for (int second = 0; second < 20; second++) {
            assertThat(this.tracker.registerMovement(HOPPER_A, HOPPER_B, second, TIMEOUT)).isFalse();
            assertThat(this.tracker.registerMovement(HOPPER_B, HOPPER_C, second, TIMEOUT)).isFalse();
        }
    }

    @Test
    @DisplayName("A movement back to the source is a clock")
    void movementBackIsAClock() {
        assertThat(this.tracker.registerMovement(HOPPER_A, HOPPER_B, 0, TIMEOUT)).isFalse();
        assertThat(this.tracker.registerMovement(HOPPER_B, HOPPER_A, 1, TIMEOUT)).isTrue();
    }

    @Test
    @DisplayName("Every direction change of a running clock is reported")
    void everyCycleIsReported() {
        assertThat(this.tracker.registerMovement(HOPPER_A, HOPPER_B, 0, TIMEOUT)).isFalse();
        for (int second = 1; second < 10; second++) {
            var from = second % 2 == 0 ? HOPPER_A : HOPPER_B;
            var to = second % 2 == 0 ? HOPPER_B : HOPPER_A;
            assertThat(this.tracker.registerMovement(from, to, second, TIMEOUT))
                    .as("cycle %s", second)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("A direction change after the timeout starts over")
    void expiredMovementIsNoClock() {
        assertThat(this.tracker.registerMovement(HOPPER_A, HOPPER_B, 0, TIMEOUT)).isFalse();
        assertThat(this.tracker.registerMovement(HOPPER_B, HOPPER_A, TIMEOUT + 1, TIMEOUT)).isFalse();
        assertThat(this.tracker.registerMovement(HOPPER_A, HOPPER_B, TIMEOUT + 2, TIMEOUT)).isTrue();
    }

    @Test
    @DisplayName("Hopper pairs are tracked independently")
    void pairsAreTrackedIndependently() {
        assertThat(this.tracker.registerMovement(HOPPER_A, HOPPER_B, 0, TIMEOUT)).isFalse();
        assertThat(this.tracker.registerMovement(HOPPER_B, HOPPER_C, 1, TIMEOUT)).isFalse();
        assertThat(this.tracker.registerMovement(HOPPER_C, HOPPER_B, 2, TIMEOUT)).isTrue();
        assertThat(this.tracker.registerMovement(HOPPER_B, HOPPER_A, 3, TIMEOUT)).isTrue();
    }

    @Test
    @DisplayName("Same block coordinates in different worlds are different hoppers")
    void worldsAreNotMixedUp() {
        var otherWorldA = new BlockKey(OTHER_WORLD, HOPPER_A.x(), HOPPER_A.y(), HOPPER_A.z());
        var otherWorldB = new BlockKey(OTHER_WORLD, HOPPER_B.x(), HOPPER_B.y(), HOPPER_B.z());

        assertThat(this.tracker.registerMovement(HOPPER_A, HOPPER_B, 0, TIMEOUT)).isFalse();
        assertThat(this.tracker.registerMovement(otherWorldB, otherWorldA, 1, TIMEOUT)).isFalse();
        assertThat(this.tracker.trackedPairs()).isEqualTo(2);
    }

    @Test
    @DisplayName("A movement into itself is ignored")
    void selfMovementIsIgnored() {
        assertThat(this.tracker.registerMovement(HOPPER_A, HOPPER_A, 0, TIMEOUT)).isFalse();
        assertThat(this.tracker.trackedPairs()).isZero();
    }

    @Test
    @DisplayName("A forgotten pair starts over")
    void forgottenPairStartsOver() {
        assertThat(this.tracker.registerMovement(HOPPER_A, HOPPER_B, 0, TIMEOUT)).isFalse();
        this.tracker.forget(HOPPER_B, HOPPER_A);
        assertThat(this.tracker.trackedPairs()).isZero();
        assertThat(this.tracker.registerMovement(HOPPER_B, HOPPER_A, 1, TIMEOUT)).isFalse();
    }

    @Test
    @DisplayName("Expired pairs are removed from memory")
    void expiredPairsArePurged() {
        for (int hopper = 0; hopper < 600; hopper++) {
            var source = new BlockKey(WORLD, hopper, 64, 0);
            var destination = new BlockKey(WORLD, hopper, 64, 1);
            this.tracker.registerMovement(source, destination, 0, TIMEOUT);
        }
        assertThat(this.tracker.trackedPairs()).isEqualTo(600);

        this.tracker.registerMovement(HOPPER_A, HOPPER_B, TIMEOUT * 10, TIMEOUT);
        // The purge runs every 512 movements, so it has not been triggered again yet.
        assertThat(this.tracker.trackedPairs()).isEqualTo(601);

        for (int hopper = 0; hopper < 512; hopper++) {
            this.tracker.registerMovement(HOPPER_A, HOPPER_B, TIMEOUT * 10, TIMEOUT);
        }
        assertThat(this.tracker.trackedPairs()).isEqualTo(1);
    }

    @Test
    @DisplayName("Both movement directions share the same pair")
    void pairOrderIsStable() {
        assertThat(HopperPair.of(HOPPER_A, HOPPER_B)).isEqualTo(HopperPair.of(HOPPER_B, HOPPER_A));
        assertThat(HopperPair.of(HOPPER_B, HOPPER_A).first()).isEqualTo(HOPPER_A);
    }
}
