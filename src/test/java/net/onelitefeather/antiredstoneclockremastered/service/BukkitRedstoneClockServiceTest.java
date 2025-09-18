package net.onelitefeather.antiredstoneclockremastered.service;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.model.RedstoneClock;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.BukkitRedstoneClockService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Implementation tests for BukkitRedstoneClockService.
 * Tests the core functionality of redstone clock detection and management.
 *
 * @author OneLiteFeatherNET
 * @since 2.2.0
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BukkitRedstoneClockService Implementation Tests")
class BukkitRedstoneClockServiceTest {

    private ServerMock server;
    private AntiRedstoneClockRemastered plugin;
    private RedstoneClockService redstoneClockService;
    private WorldMock world;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AntiRedstoneClockRemastered.class);
        
        // Create test world and location
        world = server.addSimpleWorld("testworld");
        testLocation = new Location(world, 100, 64, 100);
        
        // Create service instance
        redstoneClockService = new BukkitRedstoneClockService(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Should initialize service with correct configuration values")
    void shouldInitializeWithCorrectConfiguration() {
        // Given - Service is created in setUp()
        
        // When & Then - Service should be properly initialized
        assertThat(redstoneClockService).isNotNull();
        
        // Verify default configuration values are loaded
        assertThat(redstoneClockService.getRedstoneClocks()).isEmpty();
        assertThat(redstoneClockService.getRedstoneClockLocations()).isEmpty();
    }

    @Test
    @DisplayName("Should detect redstone clock at location")
    void shouldDetectRedstoneClockAtLocation() {
        // Given
        BlockMock block = world.getBlockAt(testLocation);
        block.setType(Material.REPEATER);
        
        // When
        redstoneClockService.checkAndUpdateClockStateWithActive(testLocation);
        
        // Then
        assertThat(redstoneClockService.containsLocation(testLocation)).isTrue();
        
        RedstoneClock clock = redstoneClockService.getClockByLocation(testLocation);
        assertThat(clock).isNotNull();
        assertThat(clock.getLocation()).isEqualTo(testLocation);
        assertThat(clock.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should handle multiple clock locations")
    void shouldHandleMultipleClockLocations() {
        // Given
        Location location1 = new Location(world, 100, 64, 100);
        Location location2 = new Location(world, 200, 64, 200);
        Location location3 = new Location(world, 300, 64, 300);
        
        // When
        redstoneClockService.checkAndUpdateClockStateWithActive(location1);
        redstoneClockService.checkAndUpdateClockStateWithActive(location2);
        redstoneClockService.checkAndUpdateClockStateWithActive(location3);
        
        // Then
        assertThat(redstoneClockService.getRedstoneClocks()).hasSize(3);
        assertThat(redstoneClockService.getRedstoneClockLocations()).hasSize(3);
        
        assertThat(redstoneClockService.containsLocation(location1)).isTrue();
        assertThat(redstoneClockService.containsLocation(location2)).isTrue();
        assertThat(redstoneClockService.containsLocation(location3)).isTrue();
    }

    @Test
    @DisplayName("Should return null for non-existent clock location")
    void shouldReturnNullForNonExistentLocation() {
        // Given
        Location nonExistentLocation = new Location(world, 999, 64, 999);
        
        // When
        RedstoneClock clock = redstoneClockService.getClockByLocation(nonExistentLocation);
        
        // Then
        assertThat(clock).isNull();
        assertThat(redstoneClockService.containsLocation(nonExistentLocation)).isFalse();
    }

    @Test
    @DisplayName("Should handle same location multiple times")
    void shouldHandleSameLocationMultipleTimes() {
        // Given
        Location location = new Location(world, 100, 64, 100);
        
        // When
        redstoneClockService.checkAndUpdateClockStateWithActive(location);
        redstoneClockService.checkAndUpdateClockStateWithActive(location);
        redstoneClockService.checkAndUpdateClockStateWithActive(location);
        
        // Then - Should only have one clock entry
        assertThat(redstoneClockService.getRedstoneClocks()).hasSize(1);
        assertThat(redstoneClockService.containsLocation(location)).isTrue();
        
        RedstoneClock clock = redstoneClockService.getClockByLocation(location);
        assertThat(clock).isNotNull();
        assertThat(clock.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should maintain thread safety with concurrent access")
    void shouldMaintainThreadSafety() throws InterruptedException {
        // Given
        int numThreads = 10;
        int locationsPerThread = 5;
        Thread[] threads = new Thread[numThreads];
        
        // When - Create multiple threads adding clock locations concurrently
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < locationsPerThread; j++) {
                    Location location = new Location(world, threadId * 100, 64, j * 100);
                    redstoneClockService.checkAndUpdateClockStateWithActive(location);
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then - All locations should be registered
        assertThat(redstoneClockService.getRedstoneClocks()).hasSize(numThreads * locationsPerThread);
        assertThat(redstoneClockService.getRedstoneClockLocations()).hasSize(numThreads * locationsPerThread);
    }

    @Test
    @DisplayName("Should provide immutable collections")
    void shouldProvideImmutableCollections() {
        // Given
        redstoneClockService.checkAndUpdateClockStateWithActive(testLocation);
        
        // When
        var clocks = redstoneClockService.getRedstoneClocks();
        var locations = redstoneClockService.getRedstoneClockLocations();
        
        // Then - Collections should be immutable or defensive copies
        assertThat(clocks).isNotEmpty();
        assertThat(locations).isNotEmpty();
        
        // Verify collections are not the same instance as internal collections
        // (This protects against external modification)
        assertThat(clocks).isNotSameAs(redstoneClockService.getRedstoneClocks());
        assertThat(locations).isNotSameAs(redstoneClockService.getRedstoneClockLocations());
    }
}