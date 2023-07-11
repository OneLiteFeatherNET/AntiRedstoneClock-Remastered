package net.onelitefeather.antiredstoneclockremastered;

import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiRedstoneClockRemastered extends JavaPlugin {

    private CheckTPS tps;

    @Override
    public void onEnable() {
        enableTPSChecker();
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
}
