package net.onelitefeather.antiredstoneclockremastered.injection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for the translation service selection.
 *
 * <p>The plugin picks between the legacy and the modern translation service based on the
 * Minecraft version the server reports. Minecraft version ids are not strict semver -
 * {@code "26.2"} and {@code "1.21"} have no patch component - so parsing them strictly
 * fails, the check falls through to its "cannot tell" branch and hands out the legacy
 * service. On modern Adventure that service blows up with an {@code IllegalAccessError},
 * taking the whole plugin down at load time.</p>
 *
 * @see <a href="https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/issues/267">#267</a>
 */
@DisplayName("TranslationModule version detection")
class TranslationModuleVersionTest {

    @Nested
    @DisplayName("Versions without a patch component")
    class TwoComponentVersions {

        /**
         * The exact reproduction of #267: a server on 26.2 was handed the legacy service.
         * 26.0 and 26.1 are in supportedMinecraftVersions and fail the same way.
         */
        @ParameterizedTest(name = "{0} must use the modern translation service")
        @ValueSource(strings = {"26", "26.0", "26.1", "26.2"})
        @DisplayName("Two-component ids above 1.21.4 are not legacy")
        void twoComponentVersionsAreNotLegacy(String minecraftVersionId) {
            assertThat(TranslationModule.isLegacyVersion(minecraftVersionId)).isFalse();
        }
    }

    @Nested
    @DisplayName("Versions with a patch component")
    class ThreeComponentVersions {

        @ParameterizedTest(name = "{0} uses the modern translation service")
        @ValueSource(strings = {"1.21.4", "1.21.8", "1.21.11", "26.1.1", "26.1.2"})
        @DisplayName("1.21.4 and later are not legacy")
        void modernVersionsAreNotLegacy(String minecraftVersionId) {
            assertThat(TranslationModule.isLegacyVersion(minecraftVersionId)).isFalse();
        }

        @ParameterizedTest(name = "{0} uses the legacy translation service")
        @ValueSource(strings = {"1.20.6", "1.21.1", "1.21.2", "1.21.3"})
        @DisplayName("1.21.3 and earlier stay legacy")
        void oldVersionsStayLegacy(String minecraftVersionId) {
            assertThat(TranslationModule.isLegacyVersion(minecraftVersionId)).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("1.21 stays legacy even though it has no patch component")
        void plainOneTwentyOneStaysLegacy() {
            // Parses to 1.21.0, which is below 1.21.4 - legacy is correct here.
            assertThat(TranslationModule.isLegacyVersion("1.21")).isTrue();
        }

        @ParameterizedTest(name = "pre-release {0} is not legacy")
        @ValueSource(strings = {"26.2-pre1", "1.21.5-rc1"})
        @DisplayName("Pre-release builds of modern versions are not legacy")
        void preReleaseBuildsAreNotLegacy(String minecraftVersionId) {
            assertThat(TranslationModule.isLegacyVersion(minecraftVersionId)).isFalse();
        }

        @Test
        @DisplayName("An unparseable id falls back to legacy")
        void unparseableFallsBackToLegacy() {
            // Better a degraded translation service than a hard failure on something
            // we genuinely cannot interpret.
            assertThat(TranslationModule.isLegacyVersion("not-a-version")).isTrue();
        }
    }
}
