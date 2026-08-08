package net.onelitefeather.antiredstoneclockremastered.service.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Routes the debug messages of this plugin into an own log file.
 *
 * <p>This uses the Log4j2 setup the server already runs on: a rolling file appender is attached to
 * the logger of this plugin's package, and the appenders the server configured for itself are
 * re-attached at {@link Level#INFO}. Debug messages therefore end up in
 * {@code debug-logs/latest.log} only, while warnings and errors keep showing up in the server log
 * and are written to the plugin log as well.</p>
 *
 * <p>While debug logging is off, the plugin logger simply inherits the server's log level, so
 * {@code LOGGER.debug(...)} calls cost nothing more than a level check.</p>
 *
 * @author OneLiteFeather
 * @version 1.0.0
 * @since 2.9.0
 */
public final class DebugLogging {

    /**
     * Name of the log file that is always the current one.
     */
    public static final String LATEST_LOG_NAME = "latest.log";

    /**
     * Package every logger of this plugin lives under.
     */
    static final String PLUGIN_LOGGER_NAME = "net.onelitefeather.antiredstoneclockremastered";

    static final String APPENDER_NAME = "AntiRedstoneClockRemasteredDebugLog";

    private static final String FILE_PATTERN = "%d{yyyy-MM-dd}-%i.log.gz";
    private static final String PATTERN = "[%d{HH:mm:ss.SSS}] [%t/%level] [%logger{1}] %msg%n%throwable";
    private static final String MAX_FILE_SIZE = "10 MB";

    private DebugLogging() {
        // Utility class
    }

    /**
     * Sends every debug message of this plugin to {@code latest.log} inside the given directory.
     *
     * @param directory the directory the log files are written to
     * @param keepFiles how many rolled over files are kept
     * @return {@code false} when the server does not run on Log4j2 and nothing could be set up
     */
    public static synchronized boolean enable(@NotNull Path directory, int keepFiles) {
        var context = LogManager.getContext(false);
        if (!(context instanceof LoggerContext loggerContext)) return false;

        var configuration = loggerContext.getConfiguration();
        var appender = createAppender(configuration, directory, keepFiles);
        appender.start();
        configuration.addAppender(appender);

        // additivity is off, otherwise the debug messages would bubble up into the server log
        var loggerConfig = new LoggerConfig(PLUGIN_LOGGER_NAME, Level.DEBUG, false);
        loggerConfig.addAppender(appender, Level.DEBUG, null);
        for (var serverAppender : configuration.getRootLogger().getAppenders().values()) {
            loggerConfig.addAppender(serverAppender, Level.INFO, null);
        }

        configuration.addLogger(PLUGIN_LOGGER_NAME, loggerConfig);
        loggerContext.updateLoggers();
        return true;
    }

    /**
     * Stops writing the plugin log and hands the loggers back to the server configuration.
     */
    public static synchronized void disable() {
        var context = LogManager.getContext(false);
        if (!(context instanceof LoggerContext loggerContext)) return;

        var configuration = loggerContext.getConfiguration();
        if (!isEnabled(configuration)) return;

        configuration.removeLogger(PLUGIN_LOGGER_NAME);

        Appender appender = configuration.getAppender(APPENDER_NAME);
        if (configuration instanceof AbstractConfiguration abstractConfiguration) {
            abstractConfiguration.removeAppender(APPENDER_NAME);
        } else if (appender != null) {
            appender.stop();
        }
        if (appender instanceof RollingRandomAccessFileAppender fileAppender) {
            // Stopping the appender only drops a reference to the manager, the file stays open until
            // the manager itself is closed - on Windows it would stay locked otherwise.
            fileAppender.getManager().close();
        }

        loggerContext.updateLoggers();
    }

    /**
     * @return whether the plugin log is currently being written
     */
    public static synchronized boolean isEnabled() {
        var context = LogManager.getContext(false);
        return context instanceof LoggerContext loggerContext && isEnabled(loggerContext.getConfiguration());
    }

    private static boolean isEnabled(@NotNull Configuration configuration) {
        return configuration.getLoggers().containsKey(PLUGIN_LOGGER_NAME);
    }

    private static @NotNull RollingRandomAccessFileAppender createAppender(@NotNull Configuration configuration,
                                                                          @NotNull Path directory, int keepFiles) {
        var layout = PatternLayout.newBuilder()
                .withConfiguration(configuration)
                .withPattern(PATTERN)
                .build();

        var strategy = DefaultRolloverStrategy.newBuilder()
                .withMax(Integer.toString(Math.max(keepFiles, 1)))
                .withConfig(configuration)
                .build();

        // Roll over on every server start, so each start gets its own file, and on size to keep them readable
        var policy = CompositeTriggeringPolicy.createPolicy(
                OnStartupTriggeringPolicy.createPolicy(1),
                SizeBasedTriggeringPolicy.createPolicy(MAX_FILE_SIZE));

        return RollingRandomAccessFileAppender.newBuilder()
                .withFileName(directory.resolve(LATEST_LOG_NAME).toString())
                .withFilePattern(directory.resolve(FILE_PATTERN).toString())
                .withPolicy(policy)
                .withStrategy(strategy)
                .withName(APPENDER_NAME)
                .withLayout(layout)
                .withConfiguration(configuration)
                .build();
    }
}
