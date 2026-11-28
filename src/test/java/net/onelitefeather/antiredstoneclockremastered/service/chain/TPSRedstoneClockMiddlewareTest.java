package net.onelitefeather.antiredstoneclockremastered.service.chain;

import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.CheckContext;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.EventType;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.ResultState;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that clock checks are skipped while the measured TPS is outside the configured range.
 */
class TPSRedstoneClockMiddlewareTest {

    private final List<CheckContext> forwarded = new ArrayList<>();
    private CheckTPS checkTPS;
    private RedstoneClockMiddleware chain;
    private CheckContext context;

    @BeforeEach
    void setUp() {
        this.checkTPS = mock(CheckTPS.class);
        var next = new RedstoneClockMiddleware() {
            @Override
            public ResultState check(CheckContext context) {
                forwarded.add(context);
                return ResultState.ONLY_NOTIFY;
            }
        };
        this.chain = RedstoneClockMiddleware.link(new TPSRedstoneClockMiddleware(this.checkTPS), next);
        var block = mock(Block.class);
        when(block.getLocation()).thenReturn(new Location(null, 0, 64, 0));
        this.context = CheckContext.of(block, true, EventType.REDSTONE_AND_REPEATER);
    }

    @Test
    @DisplayName("An out-of-range TPS skips the check without asking the next middleware")
    void outOfRangeTpsSkipsCheck() {
        when(this.checkTPS.isTpsOk()).thenReturn(false);
        when(this.checkTPS.getTps()).thenReturn(40L);

        var result = this.chain.check(this.context);

        assertThat(result).as("detection is paused while TPS is out of range").isEqualTo(ResultState.SKIP);
        assertThat(this.forwarded).as("the next middleware must not be called").isEmpty();
    }

    @Test
    @DisplayName("An in-range TPS passes the check on to the next middleware")
    void inRangeTpsDelegatesToNextMiddleware() {
        when(this.checkTPS.isTpsOk()).thenReturn(true);

        var result = this.chain.check(this.context);

        assertThat(result).as("the next middleware decides").isEqualTo(ResultState.ONLY_NOTIFY);
        assertThat(this.forwarded).as("the next middleware receives the same context").containsExactly(this.context);
    }
}
