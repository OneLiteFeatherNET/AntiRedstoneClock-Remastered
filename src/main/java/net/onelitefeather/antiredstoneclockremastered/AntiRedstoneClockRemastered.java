package net.onelitefeather.antiredstoneclockremastered;

import net.onelitefeather.antiredstoneclockremastered.service.RedstoneClockService;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiRedstoneClockRemastered extends JavaPlugin  {

    private RedstoneClockService redstoneClockService;

    @Override
    public void onEnable() {
        enableRedstoneClockService();
    }

    private void enableRedstoneClockService() {
        this.redstoneClockService = new RedstoneClockService(this);
    }
}
