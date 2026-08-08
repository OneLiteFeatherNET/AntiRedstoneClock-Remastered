package net.onelitefeather.antiredstoneclockremastered.translations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the shipped language files against the mistakes that are easy to make and hard to spot:
 * a key that never made it into a translation, a placeholder that got lost while rewording, and
 * apostrophes that silently disappear on their way through {@link MessageFormat}.
 */
class TranslationBundleTest {

    private static final Path RESOURCES = Paths.get("src", "main", "resources");
    private static final String BUNDLE = "antiredstoneclockremasterd";

    /** MiniMessage keeps the quotes around a click URL, MessageFormat eats them - so ignore those. */
    private static final Pattern CLICK_URL = Pattern.compile("<click:open_url:'[^']*'>");
    private static final Pattern ARGUMENT = Pattern.compile("<arg:\\d+>");
    private static final Pattern TAG = Pattern.compile("<(/?[a-z_]+)(?::[^>]*)?>");

    static List<Path> translations() throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(RESOURCES, BUNDLE + "*.properties")) {
            stream.forEach(files::add);
        }
        files.remove(defaultBundle());
        Collections.sort(files);
        return files;
    }

    private static Path defaultBundle() {
        return RESOURCES.resolve(BUNDLE + ".properties");
    }

    /**
     * Reads a bundle the same way the plugin does at runtime: UTF-8, then through MessageFormat,
     * because that is what {@code PluginTranslationRegistry} hands to MiniMessage.
     */
    private static Map<String, String> read(Path file) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        Map<String, String> values = new LinkedHashMap<>();
        properties.stringPropertyNames().forEach(key -> values.put(key, properties.getProperty(key)));
        return values;
    }

    private static List<String> matches(Pattern pattern, String value) {
        List<String> found = new ArrayList<>();
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            found.add(matcher.group());
        }
        Collections.sort(found);
        return found;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("translations")
    @DisplayName("every language ships the full set of keys")
    void containsEveryKey(Path translation) throws IOException {
        assertThat(read(translation).keySet())
                .as("keys of %s", translation.getFileName())
                .containsExactlyInAnyOrderElementsOf(read(defaultBundle()).keySet());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("translations")
    @DisplayName("placeholders and colour tags survive translation")
    void keepsPlaceholdersAndTags(Path translation) throws IOException {
        Map<String, String> defaults = read(defaultBundle());

        read(translation).forEach((key, value) -> {
            String expected = defaults.get(key);
            if (expected == null) {
                return;
            }
            assertThat(matches(ARGUMENT, value))
                    .as("<arg:n> placeholders of %s in %s", key, translation.getFileName())
                    .isEqualTo(matches(ARGUMENT, expected));
            assertThat(matches(TAG, CLICK_URL.matcher(value).replaceAll("<click>")))
                    .as("MiniMessage tags of %s in %s", key, translation.getFileName())
                    .isEqualTo(matches(TAG, CLICK_URL.matcher(expected).replaceAll("<click>")));
        });
    }

    /**
     * The registry passes every message through {@code new MessageFormat(...).toPattern()}, which
     * treats a straight apostrophe as a quoting character and drops it. Translations therefore have
     * to use the typographic apostrophe instead - otherwise "dell'ultima" reaches the player as
     * "dellultima".
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("translations")
    @DisplayName("text is not mangled by MessageFormat")
    void survivesMessageFormat(Path translation) throws IOException {
        read(translation).forEach((key, value) -> assertThat(new MessageFormat(value).toPattern())
                .as("%s in %s loses characters in MessageFormat - use the typographic apostrophe",
                        key, translation.getFileName())
                .isEqualTo(CLICK_URL.matcher(value).replaceAll(match ->
                        match.group().replace("'", ""))));
    }
}
