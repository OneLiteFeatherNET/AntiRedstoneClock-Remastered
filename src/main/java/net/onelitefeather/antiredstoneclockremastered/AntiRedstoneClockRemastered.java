package net.onelitefeather.antiredstoneclockremastered;

import net.onelitefeather.antiredstoneclockremastered.listener.PlayerListener;
import net.onelitefeather.antiredstoneclockremastered.listener.RedstoneListener;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import net.onelitefeather.antiredstoneclockremastered.service.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiRedstoneClockRemastered extends JavaPlugin  {
    private CheckTPS tps;

    private RedstoneClockService redstoneClockService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        enableTPSChecker();
        enableRedstoneClockService();
        registerEvents();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this.redstoneClockService), this);
        if (getConfig().getBoolean("check.redstoneAndRepeater", true)) {
            var repeater = Material.getMaterial("REPEATER");
            if (repeater != null) {
                getServer().getPluginManager().registerEvents(new RedstoneListener(repeater, this), this);
            } else {
                repeater = Material.getMaterial("DIODE_BLOCK_ON");
                getServer().getPluginManager().registerEvents(new RedstoneListener(repeater, this), this);
                repeater = Material.getMaterial("DIODE_BLOCK_OFF");
                getServer().getPluginManager().registerEvents(new RedstoneListener(repeater, this), this);
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
}
