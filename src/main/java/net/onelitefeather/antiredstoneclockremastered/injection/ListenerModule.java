package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import net.onelitefeather.antiredstoneclockremastered.listener.ObserverListener;
import net.onelitefeather.antiredstoneclockremastered.listener.PlayerListener;

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
        // Other listeners can be added here as they get refactored
    }
}