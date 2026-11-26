package net.onelitefeather.antiredstoneclockremastered.service.notification;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests where the notification sign is placed and how it is marked.
 */
class SignNotificationServiceTest {

    private final FakeRegionService regionService = new FakeRegionService();
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
        when(sign.getSide(any(Side.class))).thenReturn(mock(SignSide.class));
        when(this.block.getState()).thenReturn(sign);
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

    /**
     * Captures scheduled tasks so a test runs them explicitly instead of waiting for ticks.
     */
    private static final class FakeRegionService implements RegionService {
        private final List<Runnable> tasks = new ArrayList<>();

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
            return true;
        }

        void runAll() {
            List.copyOf(this.tasks).forEach(Runnable::run);
        }
    }
}
