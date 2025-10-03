package net.onelitefeather.antiredstoneclockremastered.service.impl;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.google.common.collect.Maps;
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializer;
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializerOptions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.NotificationService;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DiscordNotificationService implements NotificationService {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(DiscordNotificationService.class);
    private final AntiRedstoneClockRemastered plugin;
    private final NotificationService notificationService;
    private final WebhookClient webhookClient;

    public DiscordNotificationService(@NotNull AntiRedstoneClockRemastered plugin,
                                      @Nullable NotificationService notificationService) {
        this.plugin = plugin;
        this.notificationService = notificationService;
        this.webhookClient = this.createWebHook();
    }

    private WebhookClient createWebHook() {
        try {
            return WebhookClient.withUrl(this.plugin.getConfig().getString("notification.discord.webhook", ""));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Failed to create webhook client");
            return null;
        }
    }

    @Override
    public Component getNotificationMessage(@NotNull Location location) {
        return MiniMessage.miniMessage().deserialize(this.plugin.getConfig().getString("notification.discord.description", ""),
                Placeholder.component("x", Component.text(location.x())),
                Placeholder.component("y", Component.text(location.y())),
                Placeholder.component("z", Component.text(location.z())),
                Placeholder.component("world", Component.text(location.getWorld().getName()))
        );
    }

    @Override
    public void sendNotificationMessage(@NotNull Location location) {
        if (this.notificationService != null) {
            this.notificationService.sendNotificationMessage(location);
        }
        if (!isEnabled()) return;
        if (this.webhookClient == null) return;
        WebhookEmbedBuilder embed = new WebhookEmbedBuilder()
                .setColor(this.plugin.getConfig().getInt("notification.discord.color", 0xFF0000))
                .setDescription(DiscordSerializer.INSTANCE.serialize(getNotificationMessage(location), DiscordSerializerOptions.defaults().withEscapeMarkdown(false)))
                .setImageUrl(this.plugin.getConfig().getString("notification.discord.image", ""));
        generateFields(location).forEach(embed::addField);
        this.webhookClient.send(embed.build()).thenAccept(message -> LOGGER.debug("Sent notification to Discord"));
    }

    public List<WebhookEmbed.EmbedField> generateFields(Location location) {
        var configurationSection = this.plugin.getConfig().getMapList("notification.discord.fields");
        return configurationSection
                .stream()
                .limit(5)
                .map(map -> FieldEntry.deserialize((Map<String, Object>) map))
                .map(key -> {
            String name = key.getName();
            String value = key.getValue();
            var miniMessageValue = MiniMessage.miniMessage().deserialize(value,
                    Placeholder.component("x", Component.text(location.x())),
                    Placeholder.component("y", Component.text(location.y())),
                    Placeholder.component("z", Component.text(location.z())),
                    Placeholder.component("world", Component.text(location.getWorld().getName())));
            boolean inline = key.isInline();
            return new WebhookEmbed.EmbedField(inline, name, DiscordSerializer.INSTANCE.serialize(miniMessageValue, DiscordSerializerOptions.defaults().withEscapeMarkdown(false)));
        }).toList();
    }

    static class FieldEntry implements ConfigurationSerializable {
        private final String name;
        private final String value;
        private final boolean inline;

        private FieldEntry(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }

        @Override
        public @NotNull Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();
            result.put("name", this.name);
            result.put("value", this.value);
            result.put("inline", this.inline);
            return result;
        }
        public static FieldEntry deserialize(Map<String, Object> args) {
            String name = (String) args.get("name");
            String value = (String) args.get("value");
            boolean inline = (boolean) args.getOrDefault("inline", false);
            return new FieldEntry(name, value, inline);
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean isInline() {
            return inline;
        }
    }

    @Override
    public boolean isEnabled() {
        return this.plugin.getConfig().getStringList("notification.enabled").contains("discord");
    }
}
