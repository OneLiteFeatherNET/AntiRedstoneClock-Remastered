package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import net.onelitefeather.antiredstoneclockremastered.listener.ObserverListener;
import net.onelitefeather.antiredstoneclockremastered.listener.PistonListener;
import net.onelitefeather.antiredstoneclockremastered.listener.PlayerListener;
import net.onelitefeather.antiredstoneclockremastered.listener.SculkListener;

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
        // ComparatorListener and RedstoneListener require Material parameters
        // and are handled directly in the main plugin class for now
    }
}