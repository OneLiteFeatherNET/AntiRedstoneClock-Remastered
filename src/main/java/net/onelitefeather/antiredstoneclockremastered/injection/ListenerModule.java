package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
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
    private ComparatorListener createComparatorListener(Material material, RedstoneClockService redstoneClockService,
                                                     CheckTPS checkTPS, Plugin plugin) {
        return new ComparatorListener(material, redstoneClockService, checkTPS, plugin);
    }
    
    /**
     * Creates RedstoneListener instances for different materials.
     */
    private RedstoneListener createRedstoneListener(Material material, RedstoneClockService redstoneClockService,
                                                 CheckTPS checkTPS, Plugin plugin) {
        return new RedstoneListener(material, redstoneClockService, checkTPS, plugin);
    }

    public void registerEvents(Injector injector, Plugin plugin) {
        // Register DI-enabled listeners
        plugin.getServer().getPluginManager().registerEvents(injector.getInstance(PlayerListener.class), plugin);

        if (plugin.getConfig().getBoolean("check.observer", true)) {
            plugin.getServer().getPluginManager().registerEvents(injector.getInstance(ObserverListener.class), plugin);
        }

        if (plugin.getConfig().getBoolean("check.sculk", true)) {
            var sculk = Material.getMaterial("SCULK");
            if (sculk != null) {
                plugin.getServer().getPluginManager().registerEvents(injector.getInstance(SculkListener.class), plugin);
            }
        }

        if (plugin.getConfig().getBoolean("check.piston", true)) {
            plugin.getServer().getPluginManager().registerEvents(injector.getInstance(PistonListener.class), plugin);
        }

        // Material-dependent listeners now use dependency injection
        if (plugin.getConfig().getBoolean("check.comparator", true)) {
            var comparator = Material.getMaterial("COMPARATOR");
            if (comparator != null) {
                var listener = createComparatorListener(comparator, injector.getInstance(RedstoneClockService.class), injector.getInstance(CheckTPS.class), plugin);
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            } else {
                var listener1 = createComparatorListener(Material.getMaterial("REDSTONE_COMPARATOR_OFF"),  injector.getInstance(RedstoneClockService.class), injector.getInstance(CheckTPS.class), plugin);
                var listener2 = createComparatorListener(Material.getMaterial("REDSTONE_COMPARATOR_ON"),  injector.getInstance(RedstoneClockService.class), injector.getInstance(CheckTPS.class), plugin);
                plugin.getServer().getPluginManager().registerEvents(listener1, plugin);
                plugin.getServer().getPluginManager().registerEvents(listener2, plugin);
            }
        }

        if (plugin.getConfig().getBoolean("check.redstoneAndRepeater", true)) {
            var repeater = Material.getMaterial("REPEATER");
            if (repeater != null) {
                var listener = createRedstoneListener(repeater,  injector.getInstance(RedstoneClockService.class), injector.getInstance(CheckTPS.class), plugin);
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            } else {
                var listener1 = createRedstoneListener(Material.getMaterial("DIODE_BLOCK_ON"),  injector.getInstance(RedstoneClockService.class), injector.getInstance(CheckTPS.class), plugin);
                var listener2 = createRedstoneListener(Material.getMaterial("DIODE_BLOCK_OFF"),  injector.getInstance(RedstoneClockService.class), injector.getInstance(CheckTPS.class), plugin);
                plugin.getServer().getPluginManager().registerEvents(listener1, plugin);
                plugin.getServer().getPluginManager().registerEvents(listener2, plugin);
            }
        }
    }
}