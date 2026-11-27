package net.onelitefeather.antiredstoneclockremastered.service.decision;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.CheckContext;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.EventType;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.ResultState;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.service.notification.SignNotificationService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the removal and the warning sign together, scheduled with the delays the services use: the
 * removal one tick after the detection, the sign two ticks after it.
 */
class ClockReactionInterplayTest {

    private final TickingRegionService regionService = new TickingRegionService();
    private final RedstoneClockMiddleware middleware = mock(RedstoneClockMiddleware.class);
    private final List<Material> placedTypes = new ArrayList<>();
    private Material currentType = Material.REPEATER;
    private Block block;
    private Location location;
    private BukkitDecisionService service;

    @BeforeEach
    void setUp() {
        var config = new YamlConfiguration();
        var plugin = mock(AntiRedstoneClockRemastered.class);
        when(plugin.getConfig()).thenReturn(config);
        var world = mock(World.class);
        this.block = mock(Block.class);
        this.location = new Location(world, 7, 64, 7);
        when(world.getBlockAt(this.location)).thenReturn(this.block);
        when(this.block.getWorld()).thenReturn(world);
        // the mocked block keeps its type like a real one, so each task sees what the previous one did
        when(this.block.getType()).thenAnswer(invocation -> this.currentType);
        when(this.block.isEmpty()).thenAnswer(invocation -> this.currentType == Material.AIR);
        doAnswer(invocation -> {
            this.currentType = invocation.getArgument(0);
            this.placedTypes.add(this.currentType);
            return null;
        }).when(this.block).setType(any(Material.class), anyBoolean());
        var sign = mock(Sign.class);
        when(sign.getPersistentDataContainer()).thenReturn(mock(PersistentDataContainer.class));
        when(sign.getSide(any(Side.class))).thenReturn(mock(SignSide.class));
        when(this.block.getState()).thenReturn(sign);
        when(this.block.getDrops(any(ItemStack.class))).thenReturn(List.of());

        // created while the sign target is off, so the constructor skips its server dependent material check
        var signNotification = new SignNotificationService(plugin, null, this.regionService);
        config.set("notification.enabled", List.of("sign"));
        config.set("notification.sign.material", "OAK_SIGN");
        this.service = new BukkitDecisionService(plugin, this.middleware, this.regionService, signNotification,
                mock(ItemStack.class));
    }

    private void detect(ResultState resultState) {
        when(this.middleware.check(any())).thenReturn(resultState);
        this.service.makeDecisionWithContext(CheckContext.of(this.location, this.block, true, EventType.REDSTONE_AND_REPEATER));
    }

    @Test
    @DisplayName("With auto break and the sign target the clock is removed and the sign takes its place")
    void autoBreakRemovesClockAndPlacesSign() {
        detect(ResultState.REMOVE_AND_DROP);
        this.regionService.tick();
        this.regionService.tick();

        assertThat(this.placedTypes)
                .as("the clock is removed on the first tick, the sign placed on the emptied position on the second")
                .containsExactly(Material.AIR, Material.OAK_SIGN);
    }

    @Test
    @DisplayName("Reporting only keeps the component and places no sign")
    void onlyNotifyKeepsComponentWithoutSign() {
        detect(ResultState.ONLY_NOTIFY);
        this.regionService.tick();
        this.regionService.tick();

        assertThat(this.placedTypes).as("report only must not change the world").isEmpty();
        assertThat(this.currentType).as("the component is still there").isEqualTo(Material.REPEATER);
    }

    /**
     * Runs scheduled tasks when their tick is reached, in the order they were scheduled.
     * The delay-less variant waits one tick like the Bukkit implementation.
     */
    private static final class TickingRegionService implements RegionService {
        private final List<Scheduled> tasks = new ArrayList<>();
        private long currentTick;

        private record Scheduled(long dueTick, Runnable task) {
        }

        @Override
        public void executeInRegion(Location location, Runnable task) {
            executeInRegion(location, task, 1);
        }

        @Override
        public void executeInRegion(Location location, Runnable task, long delay) {
            this.tasks.add(new Scheduled(this.currentTick + delay, task));
        }

        @Override
        public boolean isRegionOwner(Location location) {
            return true;
        }

        void tick() {
            this.currentTick++;
            var due = this.tasks.stream().filter(scheduled -> scheduled.dueTick() <= this.currentTick).toList();
            this.tasks.removeAll(due);
            due.forEach(scheduled -> scheduled.task().run());
        }
    }
}
