package net.onelitefeather.antiredstoneclockremastered.service.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.injection.PlatformModule;
import net.onelitefeather.antiredstoneclockremastered.service.api.NotificationService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class SignNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignNotificationService.class);
    private final AntiRedstoneClockRemastered plugin;
    private final NotificationService notificationService;
    private final PlatformModule.RegionService regionService;

    public SignNotificationService(@NotNull AntiRedstoneClockRemastered plugin,
                                   @Nullable NotificationService notificationService,
                                   @NotNull PlatformModule.RegionService regionService) {
        this.plugin = plugin;
        this.notificationService = notificationService;
        this.regionService = regionService;
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
            LOGGER.info("Sending sign notification at {}", location);
            var blockMaterialString = this.plugin.getConfig().getString("notification.sign.material", "OAK_SIGN");
            var blockMaterial = Optional.ofNullable(Material.matchMaterial(blockMaterialString)).orElse(Material.OAK_SIGN);
            var block = location.getWorld().getBlockAt(location);
            block.setType(blockMaterial, false);
            var state = block.getState();
            LOGGER.info("Block state is {}", state);
            if (state instanceof Sign sign) {
                var side = sign.getSide(Side.BACK);
                var lines = this.plugin.getConfig().getStringList("notification.sign.back")
                        .stream()
                        .map(MiniMessage.miniMessage()::deserialize)
                        .limit(4)
                        .toList();
                for (var i = 0; i < lines.size(); i++) {
                    side.line(i, lines.get(i));
                }
                lines = this.plugin.getConfig().getStringList("notification.sign.front")
                        .stream()
                        .map(MiniMessage.miniMessage()::deserialize)
                        .limit(4)
                        .toList();
                side = sign.getSide(Side.FRONT);
                for (var i = 0; i < lines.size(); i++) {
                    side.line(i, lines.get(i));
                }
            }
            LOGGER.info("Updating sign at {}", location);
            block.getState().update();
        });

    }

    @Override
    public boolean isEnabled() {
        return this.plugin.getConfig().getStringList("notification.enabled").contains("sign");
    }
}
