package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import net.onelitefeather.antiredstoneclockremastered.listener.*;
import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

/**
 * Guice module for listener dependencies.
 *
 * @author TheMeinerLP
 * @since 2.2.0
 * @version 1.0.0
 */
public final class ListenerModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(PlayerListener.class);
        bind(ObserverListener.class);
        bind(SculkListener.class);
        bind(PistonListener.class);
    }
    
    /**
     * Creates ComparatorListener instances for different materials.
     */
    public ComparatorListener createComparatorListener(Material material, RedstoneClockService redstoneClockService, 
                                                     CheckTPS checkTPS, Plugin plugin) {
        return new ComparatorListener(material, redstoneClockService, checkTPS, plugin);
    }
    
    /**
     * Creates RedstoneListener instances for different materials.
     */
    public RedstoneListener createRedstoneListener(Material material, RedstoneClockService redstoneClockService, 
                                                 CheckTPS checkTPS, Plugin plugin) {
        return new RedstoneListener(material, redstoneClockService, checkTPS, plugin);
    }

    public void registerEvents() {

    }
}