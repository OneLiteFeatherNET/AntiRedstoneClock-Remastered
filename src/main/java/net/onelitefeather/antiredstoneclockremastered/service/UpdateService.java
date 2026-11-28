package net.onelitefeather.antiredstoneclockremastered.service;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.SchedulerService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.function.Supplier;

@Singleton
public final class UpdateService implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateService.class);
    private static final long UPDATE_CHECK_INTERVAL_TICKS = Long.getLong("ARCR_UPDATE_SERVICE_SCHEDULE", 20 * 60 * 60 * 3L); // 3 hours
    private static final int MAX_RETRY_COUNT = Integer.getInteger("ARCR_UPDATE_SERVICE_MAX_RETRY", 5);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final String DOWNLOAD_URL = "https://hangar.papermc.io/OneLiteFeather/AntiRedstoneClock-Remastered/versions/%s";
    private final HttpClient hangarClient;
    private final Version localVersion;
    private final SchedulerService schedulerService;
    private final ComponentLogger consoleLogger;
    private final Supplier<? extends Collection<? extends Player>> onlinePlayers;
    private Version remoteVersion;
    private ScheduledTask scheduler;
    private int retryCount = 0;

    @Inject
    public UpdateService(AntiRedstoneClockRemastered antiRedstoneClockRemastered, SchedulerService schedulerService) {
        this(Version.parse(antiRedstoneClockRemastered.getPluginMeta().getVersion()), schedulerService,
                createHangarClient(), antiRedstoneClockRemastered.getComponentLogger(), Bukkit::getOnlinePlayers);
    }

    UpdateService(Version localVersion, SchedulerService schedulerService, HttpClient hangarClient,
                  ComponentLogger consoleLogger, Supplier<? extends Collection<? extends Player>> onlinePlayers) {
        this.localVersion = localVersion;
        this.schedulerService = schedulerService;
        this.hangarClient = hangarClient;
        this.consoleLogger = consoleLogger;
        this.onlinePlayers = onlinePlayers;
    }

    static HttpClient createHangarClient() {
        return HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
    }


    @Override
    public void run() {
        if (retryCount >= MAX_RETRY_COUNT) {
            LOGGER.error("Max retry count reached for update check, stopping further attempts");
            LOGGER.error("Please check your internet connection or https://hangar.papermc.io/ status.");
            if (this.scheduler != null) {
                this.scheduler.cancel();
            }
            return;
        }
        var remoteVersion = getNewerVersion();
        if (remoteVersion != null) {
            boolean firstFound = !remoteVersion.equals(this.remoteVersion);
            this.remoteVersion = remoteVersion;
            if (firstFound) {
                notifyConsole(this.consoleLogger);
            }
            for (Player onlinePlayer : this.onlinePlayers.get()) {
                if (onlinePlayer.isOp() || onlinePlayer.hasPermission(Constants.PERMISSION_NOTIFY_UPDATE)) {
                    notifyPlayer(localVersion, remoteVersion, onlinePlayer);
                }
            }
        }
    }

    public void notifyConsole(ComponentLogger logger) {
        if (this.remoteVersion != null && this.remoteVersion.isHigherThan(this.localVersion)) {
            logger.warn(Component.translatable("antiredstoneclockremastered.notify.update.console")
                    .arguments(Component.text(localVersion.toString()),
                            Component.text(remoteVersion.toString()),
                            Component.text(DOWNLOAD_URL.formatted(remoteVersion.toString()))
                    ));
        }
    }


    public void notifyPlayer(Player player) {
        if (this.remoteVersion != null && this.remoteVersion.isHigherThan(this.localVersion)) {
            notifyPlayer(this.localVersion, this.remoteVersion, player);
        }
    }


    private void notifyPlayer(Version localVersion, Version remoteVersion, Player player) {
        player.sendMessage(Component.translatable("antiredstoneclockremastered.notify.update.player")
                .arguments(AntiRedstoneClockRemastered.PREFIX,
                        Component.text(localVersion.toString()),
                        Component.text(remoteVersion.toString())
                ));
    }

    public void schedule() {
        this.scheduler = schedulerService.runTaskTimerAsynchronously(scheduledTask -> this.run(), 0, UPDATE_CHECK_INTERVAL_TICKS);
    }


    @Nullable
    private Version getNewerVersion() {
        try {
            HttpResponse<String> httpResponse = hangarClient.send(Constants.LATEST_RELEASE_VERSION_REQUEST, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() != 200) {
                LOGGER.error("Failed to check for updates, status code: {}", httpResponse.statusCode());
                retryCount++;
                return null;
            }
            this.retryCount = 0;
            Version remoteVersion = Version.parse(httpResponse.body());
            if (remoteVersion.isHigherThan(this.localVersion)) {
                return remoteVersion;
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Something went wrong to check updates", e);
        } catch (ParseException e) {
            LOGGER.error("Failed to check for updates, Hangar returned an invalid version", e);
        }
        return null;
    }

    public void shutdown() {
        this.hangarClient.shutdownNow();
        this.scheduler.cancel();
    }
}
