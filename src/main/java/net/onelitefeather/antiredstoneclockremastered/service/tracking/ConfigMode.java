package net.onelitefeather.antiredstoneclockremastered.service.tracking;

import net.kyori.adventure.text.Component;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Enumeration for configuration modes.
 *
 * @author TheMeinerLP
 * @since 2.5.0
 * @version 1.0.0
 */
public enum ConfigMode {
    DYNAMIC(Component.translatable("antiredstoneclockremastered.notify.checkmode.dynamic").arguments(AntiRedstoneClockRemastered.PREFIX)),
    STATIC(Component.translatable("antiredstoneclockremastered.notify.checkmode.static").arguments(AntiRedstoneClockRemastered.PREFIX));

    private static final ConfigMode[] VALUES = values();

    private final Component enableMessage;

    ConfigMode(Component enableMessage) {
        this.enableMessage = enableMessage;
    }

    public Component getEnableMessage() {
        return enableMessage;
    }

    public static ConfigMode getByName(String name) {
        for (ConfigMode mode : VALUES) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return STATIC;
    }

    public static ConfigMode getEnum(Configuration configuration, String path, ConfigMode defaultMode) {
        String modeName = configuration.getString(path, defaultMode.name());
        return getByName(modeName);
    }
}
