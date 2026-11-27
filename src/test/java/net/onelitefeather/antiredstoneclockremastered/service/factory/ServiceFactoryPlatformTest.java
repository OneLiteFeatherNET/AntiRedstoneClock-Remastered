package net.onelitefeather.antiredstoneclockremastered.service.factory;

import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.region.BukkitRegionService;
import net.onelitefeather.antiredstoneclockremastered.service.region.FoliaRegionService;
import net.onelitefeather.antiredstoneclockremastered.service.scheduler.BukkitSchedulerService;
import net.onelitefeather.antiredstoneclockremastered.service.scheduler.FoliaSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Tests that the service factories pick the implementation matching the detected platform.
 * The test classpath contains no Folia server, so the regionized server class is really absent here;
 * presence is simulated by stubbing the detection for the duration of a single test.
 */
class ServiceFactoryPlatformTest {

    private AntiRedstoneClockRemastered plugin;

    @BeforeEach
    void setUp() {
        this.plugin = mock(AntiRedstoneClockRemastered.class);
    }

    @Test
    @DisplayName("The scheduler factory picks the Folia scheduler when Folia is detected")
    void schedulerFactoryPicksFoliaOnFolia() {
        try (MockedStatic<FoliaHelper> foliaHelper = mockStatic(FoliaHelper.class)) {
            foliaHelper.when(FoliaHelper::isFolia).thenReturn(true);

            assertThat(SchedulerServiceFactory.createService(this.plugin))
                    .as("Folia requires the Folia scheduler")
                    .isInstanceOf(FoliaSchedulerService.class);
        }
    }

    @Test
    @DisplayName("The scheduler factory picks the Bukkit scheduler when the Folia class is absent")
    void schedulerFactoryPicksBukkitWithoutFolia() {
        assertThat(SchedulerServiceFactory.createService(this.plugin))
                .as("a server without the regionized server class must use the Bukkit scheduler")
                .isInstanceOf(BukkitSchedulerService.class);
    }

    @Test
    @DisplayName("The region factory picks the Folia region service when Folia is detected")
    void regionFactoryPicksFoliaOnFolia() {
        try (MockedStatic<FoliaHelper> foliaHelper = mockStatic(FoliaHelper.class)) {
            foliaHelper.when(FoliaHelper::isFolia).thenReturn(true);

            assertThat(RegionServiceFactory.createService(this.plugin))
                    .as("Folia requires the Folia region service")
                    .isInstanceOf(FoliaRegionService.class);
        }
    }

    @Test
    @DisplayName("The region factory picks the Bukkit region service when the Folia class is absent")
    void regionFactoryPicksBukkitWithoutFolia() {
        assertThat(RegionServiceFactory.createService(this.plugin))
                .as("a server without the regionized server class must use the Bukkit region service")
                .isInstanceOf(BukkitRegionService.class);
    }
}
