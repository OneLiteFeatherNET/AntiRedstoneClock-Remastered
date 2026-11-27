package net.onelitefeather.antiredstoneclockremastered.service.factory;

import io.papermc.paper.ServerBuildInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Tests the detection of the Folia platform.
 */
class FoliaHelperTest {

    private static final String REGIONIZED_SERVER = "io.papermc.paper.threadedregions.RegionizedServer";

    @Test
    @DisplayName("Folia is detected when the regionized server class is present")
    void detectsFoliaByRegionizedServerClass() {
        assertThat(FoliaHelper.isFolia(className -> Object.class))
                .as("a server providing %s must be detected as Folia", REGIONIZED_SERVER)
                .isTrue();
    }

    @Test
    @DisplayName("Only the regionized server class is looked up")
    void looksUpOnlyTheRegionizedServerClass() {
        List<String> lookedUp = new ArrayList<>();

        FoliaHelper.isFolia(className -> {
            lookedUp.add(className);
            return Object.class;
        });

        assertThat(lookedUp)
                .as("only the Folia marker class must be looked up")
                .containsExactly(REGIONIZED_SERVER);
    }

    @Test
    @DisplayName("Folia is detected on a Minecraft version outside of 1.20 to 1.22")
    void detectsFoliaOnCurrentMinecraftVersion() {
        ServerBuildInfo buildInfo = mock(ServerBuildInfo.class);
        when(buildInfo.minecraftVersionId()).thenReturn("26.1");
        try (MockedStatic<ServerBuildInfo> serverBuildInfo = mockStatic(ServerBuildInfo.class)) {
            serverBuildInfo.when(ServerBuildInfo::buildInfo).thenReturn(buildInfo);

            assertThat(FoliaHelper.isFolia(className -> Object.class))
                    .as("Folia 26.1 provides %s and must be detected as Folia", REGIONIZED_SERVER)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Detection works where the server build info is unavailable")
    void detectionDoesNotTouchServerBuildInfo() {
        try (MockedStatic<ServerBuildInfo> serverBuildInfo = mockStatic(ServerBuildInfo.class)) {
            serverBuildInfo.when(ServerBuildInfo::buildInfo)
                    .thenThrow(new IllegalStateException("server build info is unavailable"));

            assertThat(FoliaHelper.isFolia(className -> Object.class))
                    .as("Folia must be detected without the server build info")
                    .isTrue();
            assertThat(FoliaHelper.isFolia(className -> {
                throw new ClassNotFoundException(className);
            }))
                    .as("Paper must be detected without the server build info")
                    .isFalse();
            serverBuildInfo.verifyNoInteractions();
        }
    }

    @Test
    @DisplayName("Paper is detected when the regionized server class is missing")
    void detectsPaperWithoutRegionizedServerClass() {
        FoliaHelper.ClassLookup lookup = className -> {
            throw new ClassNotFoundException(className);
        };

        assertThat(FoliaHelper.isFolia(lookup))
                .as("a server without %s must not be detected as Folia", REGIONIZED_SERVER)
                .isFalse();
    }
}
