package net.onelitefeather.antiredstoneclockremastered.listener;

import net.onelitefeather.antiredstoneclockremastered.service.api.RedstoneClockService;
import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlayerListener with dependency injection.
 * Tests dependency injection and listener initialization.
 *
 * @author OneLiteFeatherNET
 * @since 2.2.0
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerListener Unit Tests")
class PlayerListenerTest {

    @Mock
    private RedstoneClockService mockRedstoneClockService;
    
    @Mock
    private UpdateService mockUpdateService;

    @Test
    @DisplayName("Should initialize listener with injected dependencies")
    void shouldInitializeWithInjectedDependencies() {
        // When
        PlayerListener playerListener = new PlayerListener(mockRedstoneClockService, mockUpdateService);
        
        // Then
        assertThat(playerListener).isNotNull();
    }

    @Test
    @DisplayName("Should not accept null dependencies")
    void shouldNotAcceptNullDependencies() {
        // When & Then
        assertThrows(NullPointerException.class, () -> 
            new PlayerListener(null, mockUpdateService));
        assertThrows(NullPointerException.class, () -> 
            new PlayerListener(mockRedstoneClockService, null));
    }

    @Test
    @DisplayName("Should use dependency injection pattern")
    void shouldUseDependencyInjectionPattern() {
        // When
        PlayerListener listener1 = new PlayerListener(mockRedstoneClockService, mockUpdateService);
        PlayerListener listener2 = new PlayerListener(mockRedstoneClockService, mockUpdateService);
        
        // Then - Different listener instances but same dependencies
        assertThat(listener1).isNotSameAs(listener2);
        assertThat(listener1).isNotNull();
        assertThat(listener2).isNotNull();
    }
}