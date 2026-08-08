package net.onelitefeather.antiredstoneclockremastered.service.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that the plugin log is wired into the Log4j2 setup of the server and that debug messages
 * stay out of the appenders the server configured for itself.
 */
class DebugLoggingTest {

    private static final String TEST_LOGGER = DebugLogging.PLUGIN_LOGGER_NAME + ".SomeService";

    private LoggerContext context;
    private CollectingAppender serverAppender;
    private Level rootLevel;

    @BeforeEach
    void setUp() {
        this.context = (LoggerContext) LogManager.getContext(false);
        this.serverAppender = new CollectingAppender("CollectingServerAppender");
        this.serverAppender.start();

        var configuration = this.context.getConfiguration();
        configuration.addAppender(this.serverAppender);

        // Set up the root logger the way a server does it, the Log4j2 default would be ERROR
        var rootLogger = configuration.getRootLogger();
        this.rootLevel = rootLogger.getLevel();
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(this.serverAppender, null, null);
        this.context.updateLoggers();
    }

    @AfterEach
    void tearDown() {
        DebugLogging.disable();
        var rootLogger = this.context.getConfiguration().getRootLogger();
        rootLogger.removeAppender(this.serverAppender.getName());
        rootLogger.setLevel(this.rootLevel);
        this.serverAppender.stop();
        this.context.updateLoggers();
    }

    @Test
    @DisplayName("Debug messages are written to the plugin log")
    void writesDebugMessages(@TempDir Path directory) throws IOException {
        assertThat(DebugLogging.enable(directory, 3)).isTrue();

        LogManager.getLogger(TEST_LOGGER).debug("a debug message");

        assertThat(latestLog(directory)).contains("a debug message").contains("[SomeService]");
    }

    @Test
    @DisplayName("Debug messages never reach the appenders of the server")
    void keepsDebugMessagesOutOfTheServerLog(@TempDir Path directory) {
        DebugLogging.enable(directory, 3);

        var logger = LogManager.getLogger(TEST_LOGGER);
        logger.debug("a debug message");
        logger.info("an info message");
        logger.warn("a warn message");

        assertThat(this.serverAppender.messages())
                .containsExactly("an info message", "a warn message")
                .doesNotContain("a debug message");
    }

    @Test
    @DisplayName("Warnings are written to the plugin log as well")
    void copiesWarningsIntoThePluginLog(@TempDir Path directory) throws IOException {
        DebugLogging.enable(directory, 3);

        LogManager.getLogger(TEST_LOGGER).warn("a warn message", new IllegalStateException("boom"));

        assertThat(latestLog(directory))
                .contains("a warn message")
                .contains("java.lang.IllegalStateException: boom");
    }

    @Test
    @DisplayName("Loggers of other plugins are left alone")
    void doesNotTouchOtherLoggers(@TempDir Path directory) {
        DebugLogging.enable(directory, 3);

        LogManager.getLogger("com.example.OtherPlugin").warn("from somewhere else");

        assertThat(this.serverAppender.messages()).containsExactly("from somewhere else");
    }

    @Test
    @DisplayName("Enabling is reported and can be checked")
    void reportsItsState(@TempDir Path directory) {
        assertThat(DebugLogging.isEnabled()).isFalse();

        DebugLogging.enable(directory, 3);
        assertThat(DebugLogging.isEnabled()).isTrue();

        DebugLogging.disable();
        assertThat(DebugLogging.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("After disabling, the plugin logger falls back to the server configuration")
    void disableRestoresTheServerConfiguration(@TempDir Path directory) {
        DebugLogging.enable(directory, 3);
        DebugLogging.disable();

        var logger = LogManager.getLogger(TEST_LOGGER);
        logger.debug("a debug message");
        logger.warn("a warn message");

        assertThat(this.context.getConfiguration().getLoggers()).doesNotContainKey(DebugLogging.PLUGIN_LOGGER_NAME);
        assertThat(this.serverAppender.messages()).containsExactly("a warn message");
    }

    @Test
    @DisplayName("Disabling releases the log file")
    void disableReleasesTheLogFile(@TempDir Path directory) throws IOException {
        DebugLogging.enable(directory, 3);
        LogManager.getLogger(TEST_LOGGER).debug("a debug message");

        DebugLogging.disable();

        // On Windows this only succeeds once Log4j2 has closed the file
        assertThat(Files.deleteIfExists(directory.resolve(DebugLogging.LATEST_LOG_NAME))).isTrue();
    }

    @Test
    @DisplayName("The log file is created inside the given directory")
    void createsTheLogFile(@TempDir Path directory) {
        var target = directory.resolve("debug-logs");

        DebugLogging.enable(target, 3);
        LogManager.getLogger(TEST_LOGGER).debug("a debug message");
        DebugLogging.disable();

        assertThat(target.resolve(DebugLogging.LATEST_LOG_NAME)).exists();
    }

    /**
     * Reads the log file. Stopping the appender first, so its buffer ends up on disk.
     */
    private static String latestLog(Path directory) throws IOException {
        DebugLogging.disable();
        return Files.readString(directory.resolve(DebugLogging.LATEST_LOG_NAME));
    }

    /**
     * Stands in for the appenders a server has configured for its own log.
     */
    private static final class CollectingAppender extends AbstractAppender {

        private final List<String> messages = new CopyOnWriteArrayList<>();

        private CollectingAppender(String name) {
            super(name, null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            this.messages.add(event.getMessage().getFormattedMessage());
        }

        private List<String> messages() {
            return this.messages;
        }
    }
}
