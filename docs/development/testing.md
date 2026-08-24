# Testing Guide

This document provides comprehensive information about the testing infrastructure and practices in the AntiRedstoneClock-Remastered project.

## Testing Infrastructure

### Framework Stack
- **JUnit 5.10.1**: Modern testing framework with advanced features
- **Mockito 5.7.0**: Mocking framework for creating test doubles
- **AssertJ 3.24.2**: Fluent assertion library for readable test assertions
- **MockBukkit 3.128.0**: Bukkit server simulation for integration testing

### Test Configuration
- **Parallel Execution**: Disabled for MockBukkit compatibility
- **Test Reports**: JUnit XML and HTML reports generated
- **Coverage Reports**: Jacoco coverage analysis integrated

## Test Categories

### Unit Tests
Test individual components in isolation with mocked dependencies.

**Examples:**
- `DisplayActiveClocksCommandTest`: Tests command dependency injection and execution
- `DependencyInjectionIntegrationTest`: Tests DI framework integration
- `DIValidationTest`: Tests module structure and architectural patterns

### Integration Tests
Test complete workflows with MockBukkit server simulation.

**Features Tested:**
- Dependency injection framework integration
- Service layer architecture validation
- Command and listener dependency resolution

## Running Tests

### Command Line
```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "DisplayActiveClocksCommandTest"

# Run with detailed logging
./gradlew test --info
```

### IDE Integration
All tests are compatible with IntelliJ IDEA and Eclipse JUnit runners.

## Test Reports

### Location
- **HTML Reports**: `build/reports/tests/test/index.html`
- **JUnit XML**: `build/test-results/test/TEST-*.xml`
- **Coverage Reports**: `build/reports/jacoco/test/html/index.html`

### CI/CD Integration
- Test results automatically published in GitHub Actions
- Coverage reports uploaded as artifacts
- Test failures cause build failures

## Writing Tests

### Test Structure
```java
@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {
    
    @Mock
    private Dependency mockDependency;
    
    @InjectMocks
    private ExampleService service;
    
    @Test
    @DisplayName("Should perform expected behavior")
    void shouldPerformExpectedBehavior() {
        // Given
        when(mockDependency.someMethod()).thenReturn(expectedValue);
        
        // When
        Result result = service.performAction();
        
        // Then
        assertThat(result).isNotNull()
            .satisfies(r -> {
                assertThat(r.getValue()).isEqualTo(expectedValue);
                assertThat(r.isValid()).isTrue();
            });
        
        verify(mockDependency).someMethod();
    }
}
```

### Best Practices

#### Naming Conventions
- Test classes: `<ClassUnderTest>Test`
- Test methods: `should<ExpectedBehavior>When<Condition>`
- Display names: Descriptive sentences explaining the test

#### Test Organization
- **Given-When-Then**: Structure tests with clear sections
- **One Assertion Per Test**: Focus each test on a single behavior
- **Descriptive Assertions**: Use AssertJ for readable assertions

#### Mocking Guidelines
- **Mock External Dependencies**: Use `@Mock` for external dependencies
- **Spy Real Objects**: Use `@Spy` when testing partial behavior
- **Verify Interactions**: Always verify important method calls

## Dependency Injection Testing

### Testing DI Components
```java
@Test
void shouldInjectDependenciesCorrectly() {
    // Create injector with test modules
    Injector injector = Guice.createInjector(
        new TestServiceModule(),
        new TestListenerModule()
    );
    
    // Verify singleton behavior
    Service instance1 = injector.getInstance(Service.class);
    Service instance2 = injector.getInstance(Service.class);
    
    assertThat(instance1).isSameAs(instance2);
}
```

### Mocking in DI Context
- Create test modules with mocked dependencies
- Use `@TestConfiguration` equivalents for Spring-style testing
- Verify dependency injection eliminates service locator pattern

## MockBukkit Integration

### Server Setup
```java
@BeforeEach
void setUp() {
    server = MockBukkit.mock();
    // Note: We don't load the actual plugin due to final class limitations
    // Instead, we mock the plugin and test components in isolation
}

@AfterEach
void tearDown() {
    MockBukkit.unmock();
}
```

### Testing Bukkit Components
- Mock Bukkit API calls for consistent behavior
- Test event handling and command execution
- Validate plugin configuration and permissions

## Continuous Integration

### GitHub Actions Integration
- Tests run on multiple OS platforms (Ubuntu, Windows, macOS)
- Test results published with detailed reporting
- Coverage reports generated and archived
- Build fails on test failures

### Test Metrics
- **Coverage Threshold**: Maintain high code coverage
- **Test Execution Time**: Monitor test performance
- **Flaky Test Detection**: Identify and fix unstable tests

## Troubleshooting

### Common Issues

#### MockBukkit Plugin Loading
```
java.io.FileNotFoundException: Could not find file plugin.yml
```
**Solution**: Use mocked dependencies instead of loading the actual plugin class.

#### Parallel Test Execution
```
Tests failing intermittently in parallel execution
```
**Solution**: MockBukkit requires sequential execution (`maxParallelForks = 1`).

#### Dependency Injection Conflicts
```
Guice injection errors in tests
```
**Solution**: Create test-specific modules with mocked dependencies.

## Future Enhancements

### Planned Improvements
- **Performance Testing**: Add JMH benchmarks for critical paths
- **Property-Based Testing**: Integrate QuickCheck-style testing
- **Contract Testing**: Add consumer-driven contract tests
- **Mutation Testing**: Implement PIT mutation testing

### Tool Integration
- **SonarQube**: Code quality analysis
- **TestContainers**: Real server testing
- **Testcontainers**: Database integration testing

This testing infrastructure ensures the dependency injection refactoring maintains high quality and reliability while enabling future enhancements like Folia compatibility.