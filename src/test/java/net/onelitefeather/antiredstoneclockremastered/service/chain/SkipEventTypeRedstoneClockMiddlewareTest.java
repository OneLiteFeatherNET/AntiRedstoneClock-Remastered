package net.onelitefeather.antiredstoneclockremastered.service.chain;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.CheckContext;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.EventType;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.ResultState;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that the per event type switches of the config are honoured.
 */
class SkipEventTypeRedstoneClockMiddlewareTest {

    private static RedstoneClockMiddleware chainWith(String key, boolean enabled) {
        var config = new YamlConfiguration();
        config.set(key, enabled);
        var plugin = mock(AntiRedstoneClockRemastered.class);
        when(plugin.getConfig()).thenReturn(config);
        var next = new RedstoneClockMiddleware() {
            @Override
            public ResultState check(CheckContext context) {
                return ResultState.ONLY_NOTIFY;
            }
        };
        return RedstoneClockMiddleware.link(new SkipEventTypeRedstoneClockMiddleware(plugin), next);
    }

    private static CheckContext contextOf(EventType type) {
        var block = mock(Block.class);
        when(block.getLocation()).thenReturn(new Location(null, 0, 64, 0));
        return CheckContext.of(block, true, type);
    }

    @Test
    @DisplayName("Sculk sensors are checked when check.sculk is enabled")
    void sculkSensorIsCheckedWhenEnabled() {
        var result = chainWith("check.sculk", true).check(contextOf(EventType.SCULK_SENSOR));

        assertThat(result).as("enabled sculk check must pass on to the next middleware").isEqualTo(ResultState.ONLY_NOTIFY);
    }

    @Test
    @DisplayName("Sculk sensors are skipped when check.sculk is disabled")
    void sculkSensorIsSkippedWhenDisabled() {
        var result = chainWith("check.sculk", false).check(contextOf(EventType.SCULK_SENSOR));

        assertThat(result).as("disabled sculk check must skip").isEqualTo(ResultState.SKIP);
    }
}
