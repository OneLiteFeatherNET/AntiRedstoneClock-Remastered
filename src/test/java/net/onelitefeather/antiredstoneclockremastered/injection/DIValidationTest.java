package net.onelitefeather.antiredstoneclockremastered.injection;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation tests for dependency injection modules.
 * Ensures all modules can be instantiated and configured correctly.
 *
 * @author OneLiteFeatherNET
 * @since 2.2.0
 * @version 1.0.0
 */
@DisplayName("Dependency Injection Module Validation")
class DIValidationTest {

    private ServerMock server;
    private AntiRedstoneClockRemastered plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AntiRedstoneClockRemastered.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Service module should instantiate successfully")
    void serviceModuleShouldInstantiateSuccessfully() {
        // When
        ServiceModule serviceModule = new ServiceModule(plugin);
        
        // Then
        assertNotNull(serviceModule, "ServiceModule should be created successfully");
    }

    @Test
    @DisplayName("External support module should instantiate successfully")
    void externalSupportModuleShouldInstantiateSuccessfully() {
        // When
        ExternalSupportModule externalModule = new ExternalSupportModule(plugin);
        
        // Then
        assertNotNull(externalModule, "ExternalSupportModule should be created successfully");
    }

    @Test
    @DisplayName("Command module should instantiate successfully")
    void commandModuleShouldInstantiateSuccessfully() {
        // When
        CommandModule commandModule = new CommandModule();
        
        // Then
        assertNotNull(commandModule, "CommandModule should be created successfully");
    }

    @Test
    @DisplayName("Listener module should instantiate successfully")
    void listenerModuleShouldInstantiateSuccessfully() {
        // When
        ListenerModule listenerModule = new ListenerModule();
        
        // Then
        assertNotNull(listenerModule, "ListenerModule should be created successfully");
    }

    @Test
    @DisplayName("All modules should work together in injector")
    void allModulesShouldWorkTogetherInInjector() {
        // Given
        ServiceModule serviceModule = new ServiceModule(plugin);
        ExternalSupportModule externalModule = new ExternalSupportModule(plugin);
        CommandModule commandModule = new CommandModule();
        ListenerModule listenerModule = new ListenerModule();

        // When
        Injector injector = Guice.createInjector(
            serviceModule, 
            externalModule, 
            commandModule, 
            listenerModule
        );

        // Then
        assertNotNull(injector, "Injector should be created with all modules");
        assertTrue(injector.getAllBindings().size() > 0, "Injector should have bindings configured");
        
        System.out.println("DI Validation: All modules integrated successfully");
        System.out.println("DI Validation: Injector configured with " + injector.getAllBindings().size() + " bindings");
    }
}