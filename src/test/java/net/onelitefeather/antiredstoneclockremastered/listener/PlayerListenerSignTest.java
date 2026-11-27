package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneTrackingService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that only the plugin's own notification sign loses its drop.
 */
class PlayerListenerSignTest {

    private PlayerListener listener;

    @BeforeEach
    void setUp() {
        this.listener = new PlayerListener(mock(RedstoneTrackingService.class), mock(UpdateService.class));
    }

    private void fire(BlockBreakEvent event) throws Exception {
        var method = PlayerListener.class.getDeclaredMethod("onSignBlockBreak", BlockBreakEvent.class);
        method.setAccessible(true);
        method.invoke(this.listener, event);
    }

    private static BlockBreakEvent breakOf(Block block) {
        var event = mock(BlockBreakEvent.class);
        when(event.getBlock()).thenReturn(block);
        return event;
    }

    private static Block blockWithState(Material type, BlockState state) {
        var block = mock(Block.class);
        when(block.getType()).thenReturn(type);
        when(block.getState()).thenReturn(state);
        when(block.getState(anyBoolean())).thenReturn(state);
        // position bound metadata a former plugin sign left behind at this position
        when(block.hasMetadata("arcr-sign")).thenReturn(true);
        return block;
    }

    private static Sign signWithMarker(boolean marked) {
        var data = mock(PersistentDataContainer.class);
        when(data.has(Constants.ARCR_SIGN_KEY, PersistentDataType.BOOLEAN)).thenReturn(marked);
        var sign = mock(Sign.class);
        when(sign.getPersistentDataContainer()).thenReturn(data);
        return sign;
    }

    @Test
    @DisplayName("Breaking a non sign block at a former sign position still drops")
    void otherBlockAtFormerSignPositionDrops() throws Exception {
        var event = breakOf(blockWithState(Material.STONE, mock(BlockState.class)));

        fire(event);

        verify(event, never()).setDropItems(anyBoolean());
    }

    @Test
    @DisplayName("Breaking a sign placed by an older plugin version drops normally")
    void unmarkedSignDrops() throws Exception {
        // older versions only left position bound metadata, which the helper simulates
        var event = breakOf(blockWithState(Material.OAK_SIGN, signWithMarker(false)));

        fire(event);

        verify(event, never().description("a sign without the persistent marker is ordinary loot"))
                .setDropItems(anyBoolean());
    }

    @Test
    @DisplayName("Breaking a player placed sign drops normally")
    void playerPlacedSignDrops() throws Exception {
        var block = blockWithState(Material.OAK_SIGN, signWithMarker(false));
        when(block.hasMetadata("arcr-sign")).thenReturn(false);
        var event = breakOf(block);

        fire(event);

        verify(event, never().description("a player's own sign must drop")).setDropItems(anyBoolean());
    }

    @Test
    @DisplayName("Breaking the plugin's marked sign drops nothing")
    void markedSignDropsNothing() throws Exception {
        var block = blockWithState(Material.OAK_SIGN, signWithMarker(true));
        // the marker lives on the sign, so it also survives a restart that wipes block metadata
        when(block.hasMetadata("arcr-sign")).thenReturn(false);
        var event = breakOf(block);

        fire(event);

        verify(event).setDropItems(false);
    }
}
