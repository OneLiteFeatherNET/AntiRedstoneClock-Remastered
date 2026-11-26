package net.onelitefeather.antiredstoneclockremastered.service.decision;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.NotificationService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.CheckContext;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.EventType;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockMiddleware.ResultState;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests how a detected clock block is dropped and removed.
 */
class BukkitDecisionServiceTest {

    private final FakeRegionService regionService = new FakeRegionService();
    private final RedstoneClockMiddleware middleware = mock(RedstoneClockMiddleware.class);
    private final ItemStack tool = mock(ItemStack.class);
    private final ItemStack drop = mock(ItemStack.class);
    private World world;
    private Block block;
    private Location location;
    private BukkitDecisionService service;

    @BeforeEach
    void setUp() {
        this.world = mock(World.class);
        this.block = mock(Block.class);
        this.location = new Location(this.world, 3, 64, 3);
        when(this.world.getBlockAt(this.location)).thenReturn(this.block);
        when(this.block.getWorld()).thenReturn(this.world);
        when(this.block.getLocation()).thenReturn(this.location);
        when(this.block.getType()).thenReturn(Material.REPEATER);
        when(this.block.getState()).thenReturn(mock(BlockState.class));
        when(this.block.getDrops(this.tool)).thenReturn(List.of(this.drop));
        this.service = new BukkitDecisionService(mock(AntiRedstoneClockRemastered.class), this.middleware,
                this.regionService, mock(NotificationService.class), this.tool);
    }

    private void decide(ResultState resultState) {
        when(this.middleware.check(any())).thenReturn(resultState);
        this.service.makeDecisionWithContext(CheckContext.of(this.location, this.block, true, EventType.REDSTONE_AND_REPEATER));
    }

    @Test
    @DisplayName("Nothing is dropped before the scheduled removal task runs")
    void nothingIsDroppedInsideTheEvent() {
        decide(ResultState.REMOVE_AND_DROP);

        verify(this.world, never()).dropItem(any(Location.class), any(ItemStack.class));
        assertThat(this.regionService.tasks).as("drop and removal are one scheduled task").hasSize(1);
    }

    @Test
    @DisplayName("An unchanged clock block is dropped once and removed")
    void unchangedBlockIsDroppedOnceAndRemoved() {
        decide(ResultState.REMOVE_AND_DROP);
        this.regionService.runAll();

        verify(this.world, times(1)).dropItem(this.location, this.drop);
        verify(this.block).setType(Material.AIR, true);
    }

    @Test
    @DisplayName("A block replaced before the task runs is neither dropped nor removed")
    void replacedBlockIsLeftAlone() {
        decide(ResultState.REMOVE_AND_DROP);
        // a piston pushed the repeater away and moved stone into its place
        when(this.block.getType()).thenReturn(Material.STONE);
        this.regionService.runAll();

        verify(this.world, never()).dropItem(any(Location.class), any(ItemStack.class));
        verify(this.block, never()).setType(any(Material.class), anyBoolean());
        verify(this.block, never()).setType(any(Material.class));
    }

    @Test
    @DisplayName("A block replaced before the task runs is not removed when dropping is off")
    void replacedBlockIsNotRemovedWithoutDrop() {
        decide(ResultState.REMOVE_AND_WITHOUT_DROP);
        when(this.block.getType()).thenReturn(Material.AIR);
        this.regionService.runAll();

        verify(this.block, never()).setType(any(Material.class), anyBoolean());
    }

    @Test
    @DisplayName("Removing without drop removes an unchanged block and drops nothing")
    void removeWithoutDropDropsNothing() {
        decide(ResultState.REMOVE_AND_WITHOUT_DROP);
        this.regionService.runAll();

        verify(this.world, never()).dropItem(any(Location.class), any(ItemStack.class));
        verify(this.block).setType(Material.AIR, true);
    }

    @Test
    @DisplayName("The contents of a container clock are dropped before it is removed")
    void containerContentsAreDropped() {
        var content = mock(ItemStack.class);
        var inventory = mock(Inventory.class);
        when(inventory.getContents()).thenReturn(new ItemStack[]{content, null});
        var container = mock(Container.class);
        when(container.getInventory()).thenReturn(inventory);
        when(this.block.getType()).thenReturn(Material.HOPPER);
        when(this.block.getState()).thenReturn(container);

        decide(ResultState.REMOVE_AND_DROP);
        this.regionService.runAll();

        verify(this.world).dropItem(this.location, content);
        verify(this.world).dropItem(this.location, this.drop);
        verify(inventory).clear();
        verify(this.block).setType(Material.AIR, true);
    }

    @Test
    @DisplayName("Nothing is dropped or scheduled when the region is owned by another thread")
    void foreignRegionDropsNothing() {
        this.regionService.owner = false;

        decide(ResultState.REMOVE_AND_DROP);
        this.regionService.runAll();

        verify(this.world, never()).dropItem(any(Location.class), any(ItemStack.class));
        assertThat(this.regionService.tasks).as("no removal for a foreign region").isEmpty();
    }

    @Test
    @DisplayName("A report only result leaves the block alone")
    void onlyNotifyLeavesBlockAlone() {
        decide(ResultState.ONLY_NOTIFY);
        this.regionService.runAll();

        assertThat(this.regionService.tasks).isEmpty();
        verify(this.world, never()).dropItem(any(Location.class), any(ItemStack.class));
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
