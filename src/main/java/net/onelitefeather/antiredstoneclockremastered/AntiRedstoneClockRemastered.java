package net.onelitefeather.antiredstoneclockremastered;

import net.onelitefeather.antiredstoneclockremastered.api.PlotsquaredSupport;
import net.onelitefeather.antiredstoneclockremastered.api.WorldGuardSupport;
import net.onelitefeather.antiredstoneclockremastered.listener.*;
import net.onelitefeather.antiredstoneclockremastered.service.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import net.onelitefeather.antiredstoneclockremastered.worldguard.v6.WorldGuardLegacySupport;
import net.onelitefeather.antiredstoneclockremastered.worldguard.v7.WorldGuardModernSupport;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiRedstoneClockRemastered extends JavaPlugin {
    private CheckTPS tps;

    private RedstoneClockService redstoneClockService;
    private WorldGuardSupport worldGuardSupport;

    private PlotsquaredSupport plotsquaredSupport;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        enableWorldGuardSupport();
        enableTPSChecker();
        enableRedstoneClockService();
        registerEvents();
    }

    private void enableWorldGuardSupport() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null) {
            Bukkit.getLogger().warning("WorldGuard hasn't been found!");
            return;
        }
        @SuppressWarnings("deprecation")
        int wgVersion = Integer.parseInt(plugin.getDescription().getVersion().split("\\.")[0]);
        if (wgVersion > 6) {
            this.worldGuardSupport = new WorldGuardModernSupport(this);
        } else {
            this.worldGuardSupport = new WorldGuardLegacySupport(this);
        }

        if (this.worldGuardSupport.registerFlag()) {
            this.getLogger().info("Flag redstoneclock registered");
        } else {
            this.getLogger().severe("An error occurred while registering redstoneclock flag");
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this.redstoneClockService), this);
        if (getConfig().getBoolean("check.observer", true)) {
            getServer().getPluginManager().registerEvents(new ObserverListener(this), this);
        }
        if (getConfig().getBoolean("check.piston", true)) {
            getServer().getPluginManager().registerEvents(new PistonListener(this), this);
        }
        if (getConfig().getBoolean("check.comparator", true)) {
            var comparator = Material.getMaterial("COMPARATOR");
            if (comparator != null) {
                getServer().getPluginManager().registerEvents(new ComparatorListener(comparator, this), this);
            } else {
                getServer().getPluginManager().registerEvents(new ComparatorListener(Material.getMaterial("REDSTONE_COMPARATOR_OFF"), this), this);
                getServer().getPluginManager().registerEvents(new ComparatorListener(Material.getMaterial("REDSTONE_COMPARATOR_ON"), this), this);
            }
        }
        if (getConfig().getBoolean("check.redstoneAndRepeater", true)) {
            var repeater = Material.getMaterial("REPEATER");
            if (repeater != null) {
                getServer().getPluginManager().registerEvents(new RedstoneListener(repeater, this), this);
            } else {
                getServer().getPluginManager().registerEvents(new RedstoneListener(Material.getMaterial("DIODE_BLOCK_ON"), this), this);
                getServer().getPluginManager().registerEvents(new RedstoneListener(Material.getMaterial("DIODE_BLOCK_OFF"), this), this);
            }
        }
    }

    private void enableRedstoneClockService() {
        this.redstoneClockService = new RedstoneClockService(this);
    }

    private void enableTPSChecker() {
        this.tps = new CheckTPS(this,
                getConfig().getInt("tps.interval", 2),
                getConfig().getInt("tps.max", 20),
                getConfig().getInt("tps.min", 15)
        );
        this.tps.startCheck();
    }

    public CheckTPS getTps() {
        return tps;
    }

    public RedstoneClockService getRedstoneClockService() {
        return redstoneClockService;
    }

    public WorldGuardSupport getWorldGuardSupport() {
        return worldGuardSupport;
    }
}
