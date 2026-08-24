# Dependency Injection Implementation

This document describes the dependency injection (DI) implementation using Google Guice to improve the maintainability and extensibility of AntiRedstoneClock-Remastered.

## Overview

The plugin has been refactored to use dependency injection patterns, making it more modular and easier to test. The implementation uses Google Guice as the DI framework.

## Module Structure

### ServiceModule
Manages core plugin services:
- `RedstoneClockService` - Core redstone clock detection logic
- `UpdateService` - Plugin update checking and notifications
- `CheckTPS` - TPS monitoring functionality
- `TranslationService` - Internationalization support (provider method selects modern/legacy based on server version)

### ExternalSupportModule
Handles optional external plugin integrations:
- `WorldGuardSupport` - Integration with WorldGuard plugin (provider method selects v6/v7 based on version)
- `PlotsquaredSupport` - Integration with PlotSquared plugin (provider method selects v6/v7 based on version)

### CommandModule
Manages command classes:
- `ReloadCommand`
- `DisplayActiveClocksCommand`
- `FeatureCommand`

### ListenerModule
Manages event listeners:
- `PlayerListener`
- `ObserverListener`
- Additional listeners can be added as they get refactored

## Benefits

### 1. Improved Maintainability
- Dependencies are explicitly declared through constructor injection
- No more manual service creation scattered throughout the main class
- Clear separation of concerns between different modules

### 2. Enhanced Testability
- Services can be easily mocked for unit testing
- Dependencies are injected rather than hard-coded
- Easier to create isolated test scenarios

### 3. Better Extensibility for Folia
- Service layer is now modular and can be easily extended
- Platform-specific implementations can be swapped in via configuration
- Clear interface boundaries make it easier to add platform-specific optimizations

### 4. Reduced Coupling
- Classes depend on interfaces rather than concrete implementations
- Easier to change implementations without affecting consumers
- Better adherence to SOLID principles

## Usage Examples

### Injecting Dependencies
```java
@Singleton
public class SomeService {
    private final RedstoneClockService clockService;
    private final CheckTPS tpsChecker;
    
    @Inject
    public SomeService(RedstoneClockService clockService, CheckTPS tpsChecker) {
        this.clockService = clockService;
        this.tpsChecker = tpsChecker;
    }
}
```

### Adding New Services
To add a new service:
1. Create the service class with `@Inject` constructor
2. Add `@Singleton` if it should be a singleton
3. Bind it in the appropriate module's `configure()` method
4. Inject it where needed

### Creating Platform-Specific Implementations
```java
@Provides
@Singleton
public SomeService provideSomeService(AntiRedstoneClockRemastered plugin) {
    if (plugin.getServer().getName().equals("Folia")) {
        return new FoliaSomeService(plugin);
    } else {
        return new DefaultSomeService(plugin);
    }
}
```

## Migration Notes

- Legacy listeners that haven't been refactored yet still use manual instantiation
- External support (WorldGuard/PlotSquared) now uses provider methods for version detection
- The main plugin class is significantly simplified with most manual setup removed

## Future Improvements

1. Refactor remaining listeners to use DI
2. Add interface abstractions for better testability
3. Consider platform-specific modules for Folia support
4. Add configuration-based service selection