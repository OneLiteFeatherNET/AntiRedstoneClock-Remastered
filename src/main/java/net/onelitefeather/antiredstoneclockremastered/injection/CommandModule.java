package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import net.onelitefeather.antiredstoneclockremastered.commands.DisplayActiveClocksCommand;
import net.onelitefeather.antiredstoneclockremastered.commands.FeatureCommand;
import net.onelitefeather.antiredstoneclockremastered.commands.ReloadCommand;

/**
 * Guice module for command dependencies.
 *
 * @author TheMeinerLP
 * @since 2.2.0
 * @version 1.0.0
 */
public final class CommandModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(ReloadCommand.class);
        bind(DisplayActiveClocksCommand.class);
        bind(FeatureCommand.class);
    }
}