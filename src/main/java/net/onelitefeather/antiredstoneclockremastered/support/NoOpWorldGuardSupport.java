package net.onelitefeather.antiredstoneclockremastered.support;

import net.onelitefeather.antiredstoneclockremastered.api.AbstractWorldGuardSupport;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class NoOpWorldGuardSupport extends AbstractWorldGuardSupport {

    public NoOpWorldGuardSupport(@NotNull Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isRegionAllowed(@NotNull Location location) {
        return false;
    }

    @Override
    public String getVersion() {
        return "No WorldGuard found";
    }

    @Override
    public boolean registerFlag() {
        return true;
    }
}
