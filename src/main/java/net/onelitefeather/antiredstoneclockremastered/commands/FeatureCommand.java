package net.onelitefeather.antiredstoneclockremastered.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.RedstoneClockService;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import javax.inject.Inject;
import java.util.List;

@Command("arcm feature")
public final class FeatureCommand {

    private final AntiRedstoneClockRemastered plugin;
    private final RedstoneClockService redstoneClockService;

    @Inject
    public FeatureCommand(AntiRedstoneClockRemastered plugin, RedstoneClockService redstoneClockService) {
        this.plugin = plugin;
        this.redstoneClockService = redstoneClockService;
    }

    @Command("check observer")
    @CommandDescription("antiredstoneclockremastered.command.feature.check.toggle.observer.description")
    @Permission("antiredstoneclockremastered.command.feature.check.observer")
    public void toggleObserver(CommandSender sender) {
        plugin.getConfig().set("check.observer", !plugin.getConfig().getBoolean("check.observer"));
        plugin.saveConfig();
        redstoneClockService.reload();
        sendMessageToggleMessage(sender, plugin.getConfig().getBoolean("check.observer"));
    }

    @Command("check piston")
    @CommandDescription("antiredstoneclockremastered.command.feature.check.toggle.piston.description")
    @Permission("antiredstoneclockremastered.command.feature.check.piston")
    public void togglePiston(CommandSender sender) {
        plugin.getConfig().set("check.piston", !plugin.getConfig().getBoolean("check.piston"));
        plugin.saveConfig();
        redstoneClockService.reload();
        sendMessageToggleMessage(sender, plugin.getConfig().getBoolean("check.piston"));
    }

    @Command("check comparator")
    @CommandDescription("antiredstoneclockremastered.command.feature.check.toggle.comparator.description")
    @Permission("antiredstoneclockremastered.command.feature.check.comparator")
    public void toggleComparator(CommandSender sender) {
        plugin.getConfig().set("check.comparator", !plugin.getConfig().getBoolean("check.comparator"));
        plugin.saveConfig();
        redstoneClockService.reload();
        sendMessageToggleMessage(sender, plugin.getConfig().getBoolean("check.comparator"));
    }

    @Command("check sculk")
    @CommandDescription("antiredstoneclockremastered.command.feature.check.toggle.sculk.description")
    @Permission("antiredstoneclockremastered.command.feature.check.sculk")
    public void toggleSculk(CommandSender sender) {
        plugin.getConfig().set("check.sculk", !plugin.getConfig().getBoolean("check.sculk"));
        plugin.saveConfig();
        redstoneClockService.reload();
        sendMessageToggleMessage(sender, plugin.getConfig().getBoolean("check.sculk"));
    }

    @Command("check redstone_and_repeater")
    @CommandDescription("antiredstoneclockremastered.command.feature.check.toggle.redstone_and_repeater.description")
    @Permission("antiredstoneclockremastered.command.feature.check.redstone_and_repeater")
    public void toggleRedstoneAndRepeater(CommandSender sender) {
        plugin.getConfig().set("check.redstoneAndRepeater", !plugin.getConfig().getBoolean("check.redstoneAndRepeater"));
        plugin.saveConfig();
        redstoneClockService.reload();
        sendMessageToggleMessage(sender, plugin.getConfig().getBoolean("check.redstoneAndRepeater"));
    }

    private void sendMessageToggleMessage(CommandSender sender, boolean value) {
        if (value) {
            sender.sendMessage(Component.translatable("antiredstoneclockremastered.command.feature.check.toggle.enabled").arguments(AntiRedstoneClockRemastered.PREFIX));
        } else {
            sender.sendMessage(Component.translatable("antiredstoneclockremastered.command.feature.check.toggle.disabled").arguments(AntiRedstoneClockRemastered.PREFIX));
        }
    }

    @Command("check ignored_worlds add <world>")
    @CommandDescription("antiredstoneclockremastered.command.feature.check.world.add.description")
    @Permission("antiredstoneclockremastered.command.feature.check.world.add")
    public void addIgnoredWorld(CommandSender sender, @Argument("world") World world) {
        List<String> worlds = plugin.getConfig().getStringList("check.ignoredWorlds");
        worlds.add(world.getName());
        plugin.getConfig().set("check.ignoredWorlds", worlds);
        plugin.saveConfig();
        redstoneClockService.reload();
        sender.sendMessage(Component.translatable("antiredstoneclockremastered.command.feature.check.world.add").arguments(AntiRedstoneClockRemastered.PREFIX, Component.text(world.getName())));
    }

    @Command("check ignored_worlds remove <world>")
    @CommandDescription("antiredstoneclockremastered.command.feature.check.world.remove.description")
    @Permission("antiredstoneclockremastered.command.feature.check.world.remove")
    public void removeIgnoredWorld(CommandSender sender, @Argument("world") World world) {
        List<String> worlds = plugin.getConfig().getStringList("check.ignoredWorlds");
        worlds.remove(world.getName());
        plugin.getConfig().set("check.ignoredWorlds", worlds);
        plugin.saveConfig();
        redstoneClockService.reload();
        sender.sendMessage(Component.translatable("antiredstoneclockremastered.command.feature.check.world.remove").arguments(AntiRedstoneClockRemastered.PREFIX, Component.text(world.getName())));
    }

    @Command("check ignored_regions add <region>")
    @CommandDescription("antiredstoneclockremastered.command.feature.check.region.add.description")
    @Permission("antiredstoneclockremastered.command.feature.check.region.add")
    public void addIgnoredRegion(CommandSender sender, @Argument("region") String region) {
        List<String> regions = plugin.getConfig().getStringList("check.ignoredRegions");
        regions.add(region);
        plugin.getConfig().set("check.ignoredRegions", regions);
        plugin.saveConfig();
        redstoneClockService.reload();
        sender.sendMessage(Component.translatable("antiredstoneclockremastered.command.feature.check.region.add").arguments(AntiRedstoneClockRemastered.PREFIX, Component.text(region)));
    }

    @Command("check ignored_regions remove <region>")
    @CommandDescription("antiredstoneclockremastered.command.feature.check.region.remove.description")
    @Permission("antiredstoneclockremastered.command.feature.check.region.remove")
    public void removeIgnoredRegion(CommandSender sender, @Argument("region") String region) {
        List<String> regions = plugin.getConfig().getStringList("check.ignoredRegions");
        regions.remove(region);
        plugin.getConfig().set("check.ignoredRegions", regions);
        plugin.saveConfig();
        redstoneClockService.reload();
        sender.sendMessage(Component.translatable("antiredstoneclockremastered.command.feature.check.region.remove").arguments(AntiRedstoneClockRemastered.PREFIX, Component.text(region)));
    }

    @Command("clock autoBreak")
    @CommandDescription("antiredstoneclockremastered.command.feature.clock.toggle.autoBreak.description")
    @Permission("antiredstoneclockremastered.command.feature.clock.autoBreak")
    public void toggleAutoBreak(CommandSender sender) {
        plugin.getConfig().set("clock.autoBreak", !plugin.getConfig().getBoolean("clock.autoBreak"));
        plugin.saveConfig();
        redstoneClockService.reload();
        sendMessageToggleMessage(sender, plugin.getConfig().getBoolean("clock.autoBreak"));
    }

    @Command("clock notify_admins")
    @CommandDescription("antiredstoneclockremastered.command.feature.clock.toggle.notifyAdmins.description")
    @Permission("antiredstoneclockremastered.command.feature.clock.notifyAdmins")
    public void toggleNotifyAdmins(CommandSender sender) {
        plugin.getConfig().set("clock.notifyAdmins", !plugin.getConfig().getBoolean("clock.notifyAdmins"));
        plugin.saveConfig();
        redstoneClockService.reload();
        sendMessageToggleMessage(sender, plugin.getConfig().getBoolean("clock.notifyAdmins"));
    }

    @Command("clock notify_console")
    @CommandDescription("antiredstoneclockremastered.command.feature.clock.toggle.notifyConsole.description")
    @Permission("antiredstoneclockremastered.command.feature.clock.notifyConsole")
    public void toggleNotifyConsole(CommandSender sender) {
        plugin.getConfig().set("clock.notifyConsole", !plugin.getConfig().getBoolean("clock.notifyConsole"));
        plugin.saveConfig();
        redstoneClockService.reload();
        sendMessageToggleMessage(sender, plugin.getConfig().getBoolean("clock.notifyConsole"));
    }

    @Command("clock drop")
    @CommandDescription("antiredstoneclockremastered.command.feature.clock.toggle.drop.description")
    @Permission("antiredstoneclockremastered.command.feature.clock.drop")
    public void toggleDrop(CommandSender sender) {
        plugin.getConfig().set("clock.drop", !plugin.getConfig().getBoolean("clock.drop"));
        plugin.saveConfig();
        redstoneClockService.reload();
        sendMessageToggleMessage(sender, plugin.getConfig().getBoolean("clock.drop"));
    }

    @Command("clock endDelay <delay>")
    @CommandDescription("antiredstoneclockremastered.command.feature.clock.set.delay.description")
    @Permission("antiredstoneclockremastered.command.feature.clock.enddelay")
    public void setEndDelay(CommandSender sender,@Argument("delay") Integer endDeplay) {
        plugin.getConfig().set("clock.endDelay", endDeplay);
        plugin.saveConfig();
        redstoneClockService.reload();
        sendMessageSetMessage(sender, plugin.getConfig().getInt("clock.endDelay"));
    }

    @Command("clock maxCount <count>")
    @CommandDescription("antiredstoneclockremastered.command.feature.clock.set.maxCount.description")
    @Permission("antiredstoneclockremastered.command.feature.clock.maxCount")
    public void setMaxCount(CommandSender sender,@Argument("count") Integer count) {
        plugin.getConfig().set("clock.maxCount", count);
        plugin.saveConfig();
        redstoneClockService.reload();
        sendMessageSetMessage(sender, plugin.getConfig().getInt("clock.maxCount"));
    }

    private void sendMessageSetMessage(CommandSender sender, Integer value) {
        sender.sendMessage(Component.translatable("antiredstoneclockremastered.command.feature.clock.set").arguments(AntiRedstoneClockRemastered.PREFIX, TranslationArgument.numeric(value)));
    }

}
