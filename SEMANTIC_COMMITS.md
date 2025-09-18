# Semantic Commit History

The commits in this pull request follow semantic commit conventions:

## Commit History (Semantic Convention)
1. **refactor:** Implement Service Layer Architecture for RedstoneClockService (commit 99c9239)
   - Create RedstoneClockService interface with complete API
   - Implement BukkitRedstoneClockService with existing logic  
   - Add RedstoneClockServiceFactory for platform detection
   - Update main plugin to use service factory
   - Update listeners to use service interface
   - Remove old concrete RedstoneClockService implementation

2. **feat:** Add Folia support preparation and comprehensive documentation (commit 93b0f3b)
   - Add FoliaRedstoneClockService implementation structure
   - Create SERVICE_ARCHITECTURE.md with detailed documentation
   - Enhance factory with smart platform detection logic
   - Prepare infrastructure for future Folia implementation

3. **docs:** Add Javadoc tags and improve platform detection (commit cc8ec21)
   - Add comprehensive Javadoc tags to all service classes
   - Improve Folia detection with version-based approach
   - Add Mermaid diagram to architecture documentation
   - Enhance code documentation standards

4. **fix:** Improve code consistency and logging practices (commit 36ec6af)
   - Update Javadoc tags for consistency (author: TheMeinerLP, version: 2.2.0)
   - Add class-level SLF4J logger to RedstoneClockServiceFactory
   - Fix Mermaid diagram syntax for better compatibility
   - Apply DRY principles across service classes

All commits follow the conventional commit format: `type: description` where type is one of:
- `feat:` for new features
- `fix:` for bug fixes  
- `docs:` for documentation changes
- `refactor:` for code refactoring
- `chore:` for maintenance tasks

