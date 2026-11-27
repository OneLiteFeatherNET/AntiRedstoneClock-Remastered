package net.onelitefeather.antiredstoneclockremastered.service.notification;

import net.kyori.adventure.text.Component;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests where the notification sign is placed and how it is marked.
 */
class SignNotificationServiceTest {

    private final FakeRegionService regionService = new FakeRegionService();
    private final SignSide front = mock(SignSide.class);
    private YamlConfiguration config;
    private Block block;
    private Location location;
    private PersistentDataContainer signData;
    private SignNotificationService service;

    @BeforeEach
    void setUp() {
        var config = new YamlConfiguration();
        var plugin = mock(AntiRedstoneClockRemastered.class);
        when(plugin.getConfig()).thenReturn(config);
        var world = mock(World.class);
        this.block = mock(Block.class);
        this.location = new Location(world, 5, 64, 5);
        when(world.getBlockAt(this.location)).thenReturn(this.block);
        this.signData = mock(PersistentDataContainer.class);
        var sign = mock(Sign.class);
        when(sign.getPersistentDataContainer()).thenReturn(this.signData);
        when(sign.getSide(Side.FRONT)).thenReturn(this.front);
        when(sign.getSide(Side.BACK)).thenReturn(mock(SignSide.class));
        when(this.block.getState()).thenReturn(sign);
        this.config = config;
        // created while the sign target is off, so the constructor skips its server dependent material check
        this.service = new SignNotificationService(plugin, null, this.regionService);
        config.set("notification.enabled", List.of("sign"));
        config.set("notification.sign.material", "OAK_SIGN");
    }

    @Test
    @DisplayName("No sign replaces a block that still stands at the clock position")
    void signIsNotPlacedOverAnExistingBlock() {
        when(this.block.getType()).thenReturn(Material.REPEATER);

        this.service.sendNotificationMessage(this.location);
        this.regionService.runAll();

        verify(this.block, never()).setType(any(Material.class), anyBoolean());
        verify(this.block, never()).setType(any(Material.class));
    }

    @Test
    @DisplayName("A sign is placed when the clock position is air")
    void signIsPlacedOnAir() {
        when(this.block.getType()).thenReturn(Material.AIR);
        when(this.block.isEmpty()).thenReturn(true);

        this.service.sendNotificationMessage(this.location);
        this.regionService.runAll();

        verify(this.block).setType(Material.OAK_SIGN, false);
    }

    @Test
    @DisplayName("The placed sign carries the persistent plugin marker")
    void placedSignIsMarkedPersistently() {
        when(this.block.getType()).thenReturn(Material.AIR);
        when(this.block.isEmpty()).thenReturn(true);

        this.service.sendNotificationMessage(this.location);
        this.regionService.runAll();

        verify(this.signData).set(Constants.ARCR_SIGN_KEY, PersistentDataType.BOOLEAN, true);
    }

    @Test
    @DisplayName("The sign is placed in a scheduled task, not inside the event")
    void signIsPlacedLater() {
        when(this.block.getType()).thenReturn(Material.AIR);
        when(this.block.isEmpty()).thenReturn(true);

        this.service.sendNotificationMessage(this.location);

        verify(this.block, never()).setType(any(Material.class), anyBoolean());
        assertThat(this.regionService.tasks).hasSize(1);
    }

    @Test
    @DisplayName("A configured material that is no sign is not placed over a standing block")
    void nonSignMaterialIsNotPlacedOverABlock() {
        this.config.set("notification.sign.material", "STONE");
        when(this.block.getType()).thenReturn(Material.REPEATER);

        this.service.sendNotificationMessage(this.location);
        this.regionService.runAll();

        verify(this.block, never().description("the occupied check applies to every material"))
                .setType(any(Material.class), anyBoolean());
    }

    @Test
    @DisplayName("A configured material that is no sign is placed on an empty position")
    void nonSignMaterialIsPlacedOnAir() {
        this.config.set("notification.sign.material", "STONE");
        when(this.block.isEmpty()).thenReturn(true);
        when(this.block.getState()).thenReturn(mock(BlockState.class));

        this.service.sendNotificationMessage(this.location);
        this.regionService.runAll();

        verify(this.block, description("the configured material is placed")).setType(Material.STONE, false);
    }

    @Test
    @DisplayName("More than four configured lines are capped to the four lines of a sign")
    void linesAreCappedToFour() {
        this.config.set("notification.sign.front", List.of("1", "2", "3", "4", "5", "6"));
        when(this.block.isEmpty()).thenReturn(true);

        this.service.sendNotificationMessage(this.location);
        this.regionService.runAll();

        verify(this.front, times(Constants.MAX_SIGN_LINES).description("a sign side has four lines"))
                .line(anyInt(), any(Component.class));
        verify(this.front, never().description("a fifth line does not exist")).line(eq(4), any(Component.class));
    }

    @Test
    @DisplayName("The marker key belongs to the plugin's namespace")
    void markerKeyUsesPluginNamespace() {
        assertThat(Constants.ARCR_SIGN_KEY.getNamespace())
                .as("the namespace of a key created from the plugin named AntiRedstoneClock-Remastered")
                .isEqualTo("antiredstoneclock-remastered");
    }

    @Test
    @DisplayName("No sign task is scheduled when the region is owned by another thread")
    void foreignRegionSchedulesNothing() {
        this.regionService.owner = false;

        this.service.sendNotificationMessage(this.location);

        assertThat(this.regionService.tasks).as("a foreign region must not be changed from here").isEmpty();
    }

    /**
     * Captures scheduled tasks so a test runs them explicitly instead of waiting for ticks.
     */
    private static final class FakeRegionService implements RegionService {
        private final List<Runnable> tasks = new ArrayList<>();
        private boolean owner = true;

        @Override
        public void executeInRegion(Location location, Runnable task) {
            this.tasks.add(task);
        }

        @Override
        public void executeInRegion(Location location, Runnable task, long delay) {
            this.tasks.add(task);
        }

        @Override
        public boolean isRegionOwner(Location location) {
            return this.owner;
        }

        void runAll() {
            List.copyOf(this.tasks).forEach(Runnable::run);
        }
    }
}
