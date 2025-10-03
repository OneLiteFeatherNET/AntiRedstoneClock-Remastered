package net.onelitefeather.antiredstoneclockremastered.service.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.NotificationService;
import net.onelitefeather.antiredstoneclockremastered.service.api.RegionService;
import net.onelitefeather.antiredstoneclockremastered.utils.Constants;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class SignNotificationService implements NotificationService {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(SignNotificationService.class);
    private final AntiRedstoneClockRemastered plugin;
    private final NotificationService notificationService;
    private final RegionService regionService;
    private final FixedMetadataValue metaValue;

    public SignNotificationService(@NotNull AntiRedstoneClockRemastered plugin,
                                   @Nullable NotificationService notificationService,
                                   @NotNull RegionService regionService) {
        this.plugin = plugin;
        this.notificationService = notificationService;
        this.regionService = regionService;
        this.metaValue = new FixedMetadataValue(plugin, Constants.META_KEY_ARCR_SIGN);
        if (isEnabled()) {
            this.warnForToLongText();
        }
    }

    private void warnForToLongText() {
        var size = this.plugin.getConfig().getStringList("notification.sign.back").size();
        if (size > Constants.MAX_SIGN_LINES) {
            LOGGER.warn("Sign notification 'back' side has too many lines: {}", size);
        }
        size = this.plugin.getConfig().getStringList("notification.sign.front").size();
        if (size > Constants.MAX_SIGN_LINES) {
            LOGGER.warn("Sign notification 'front' side has too many lines: {}", size);
        }
        this.plugin.getConfig().getStringList("notification.sign.back")
                .stream()
                .map(MiniMessage.miniMessage()::stripTags)
                .filter(this::isLineToLongForSign)
                .forEach(line -> LOGGER.warn("Sign notification line is too long for back side: {}", line));
        this.plugin.getConfig().getStringList("notification.sign.front")
                .stream()
                .map(MiniMessage.miniMessage()::stripTags)
                .filter(this::isLineToLongForSign)
                .forEach(line -> LOGGER.warn("Sign notification line is too long for front side: {}", line));
    }

    private boolean isLineToLongForSign(String line) {
        return line.length() > Constants.MAX_SIGN_LINE_LENGTH;
    }

    @Override
    public Component getNotificationMessage(@NotNull Location location) {
        return null;
    }

    @Override
    public void sendNotificationMessage(@NotNull Location location) {
        if (this.notificationService != null) {
            this.notificationService.sendNotificationMessage(location);
        }
        if (!isEnabled()) return;
        if (!this.regionService.isRegionOwner(location)) return;
        this.regionService.executeInRegion(location, () -> {
            LOGGER.debug("Sending sign notification at {}", location);
            var blockMaterialString = this.plugin.getConfig().getString("notification.sign.material", "OAK_SIGN");
            var blockMaterial = Optional.ofNullable(Material.matchMaterial(blockMaterialString)).orElse(Material.OAK_SIGN);
            var block = location.getWorld().getBlockAt(location);
            block.setType(blockMaterial, false);
            var state = block.getState();
            LOGGER.debug("Block state is {}", state);
            if (state instanceof Sign sign) {
                sign.setMetadata(Constants.META_KEY_ARCR_SIGN, this.metaValue);
                var side = sign.getSide(Side.BACK);
                var lines = this.plugin.getConfig().getStringList("notification.sign.back")
                        .stream()
                        .map(MiniMessage.miniMessage()::deserialize)
                        .limit(Constants.MAX_SIGN_LINES)
                        .toList();
                for (var i = 0; i < lines.size(); i++) {
                    side.line(i, lines.get(i));
                }
                sign.update(true, false);
                lines = this.plugin.getConfig().getStringList("notification.sign.front")
                        .stream()
                        .map(MiniMessage.miniMessage()::deserialize)
                        .limit(Constants.MAX_SIGN_LINES)
                        .toList();
                side = sign.getSide(Side.FRONT);
                for (var i = 0; i < lines.size(); i++) {
                    side.line(i, lines.get(i));
                }
                sign.update(true, false);
            }
            LOGGER.debug("Updating sign at {}", location);
            block.getState().update();
        }, 2);

    }

    @Override
    public boolean isEnabled() {
        return this.plugin.getConfig().getStringList("notification.enabled").contains("sign");
    }
}
