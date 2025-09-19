# AntiRedstoneClock-Remastered
[![Crowdin](https://badges.crowdin.net/e/79ae9c901c3d260349569fca62af7b2f/localized.svg)](https://onelitefeather.crowdin.com/antiredstoneclock-remastered)

This plugin is inspired by https://gitlab.com/Trafalcraft/antiRedstoneClock

We re-created the whole code and improved everything. On top, we support Plotsquared v7 and Worldguard v7.

### Goal
The goal of this plugin is to detect redstone clocks, inform staff or console about active ones and optionally destroy / disable the redstone clocks so your server can save performance for something else. Also it can prevent players doing harm with heavy clocks, but this is more of a side effect.

### Not a goal
It's not planned to support Paper forks or spigot directly - this plugin is developed to work on Paper, if you are using a fork of paper or spigot and it doesn't work because of your fork, you are likely on your own then.
Also this plugin is not a "performance tool", it won't make your server run better directly.
Third, support from 1.13 backwards likely won't happen.

## Minecraft Version Support
Only the last 2 versions of a major Minecraft are supported.
For Minecraft version 1.19, it would be 1.19.4,
For Minecraft version 1.20, it would be 1.20.6 and so on

### Currently Supported and Tested Versions
The following Minecraft versions are actively supported and automatically tested via GitHub Actions:

- **1.20.6** - Latest 1.20.x version
- **1.21** - Initial 1.21 release  
- **1.21.3** - Tested compatibility version
- **1.21.4** - Latest tested version

> **Note**: All versions are tested automatically on every push and pull request to ensure plugin compatibility and detect potential exceptions or issues. The testing workflow verifies plugin builds, checks for critical startup errors, and runs the full unit test suite.

### Automated Testing Workflow

The repository includes a comprehensive GitHub Actions workflow (`minecraft-version-tests.yml`) that:

- **Matrix Testing**: Tests the plugin against all supported Minecraft versions simultaneously
- **Build Verification**: Ensures the plugin builds successfully for each version
- **Separated Exception Logging**: Uses custom log4j2 configurations to create separate exception logs per version
- **Exception Detection**: Scans version-specific exception logs for critical startup errors like `ClassNotFoundException`, `NoSuchMethodError`
- **Plugin Status Monitoring**: Tracks plugin loading and operational status through dedicated log files
- **Unit Testing**: Runs the complete test suite to ensure functionality
- **Artifact Collection**: Uploads build artifacts, separated logs, and test reports for debugging
- **Scheduled Testing**: Runs weekly to catch compatibility issues with new Paper builds

#### Separated Logging Features

Each Minecraft version gets its own logging configuration:
- **Exception Logs**: `run-{version}/logs/exceptions-{version}.log` - Contains only ERROR level and above
- **Plugin Status Logs**: `run-{version}/logs/plugin-status-{version}.log` - Tracks plugin lifecycle
- **General Server Logs**: `run-{version}/logs/latest.log` - Standard server output
- **Custom log4j2 Configuration**: Version-specific configurations in `run-{version}/log4j2.xml`

#### Available Gradle Tasks

For each supported Minecraft version, the following tasks are available:
- `run-{version}` - Run the server with separated logging
- `createLog4jConfig-{version}` - Generate version-specific log4j2 configuration  
- `checkPluginStatus-{version}` - Analyze logs and report plugin status

To validate the testing setup locally, run:
```bash
./validate_workflow.sh
```

## Features
- 1.20+ Support
- Java 21 only support
- Plotsquared v6 and v7 support
- WorldGuard Support
- 1.20,1.21 ready
- Clock detection
- Sculk support
- Config Migration(Soon)
- Prevent duplicated loading of anti-redstoneclock plugins

> [!CAUTION]
> The "world" world is ignored by default

## Contribution
You want to help us? Sure go for it, we would love to see your contribution! You can look for open issues or if you have a nice idea, please open an issue or ask us on discord if you can add your feature with a PR. Communication is key.

## Dependencies (soft-depend, can be used together)
- Plotsquared v7 https://github.com/IntellectualSites/PlotSquared
- Worldguard v7 https://github.com/EngineHub/WorldGuard

## Permissions:
```
antiredstoneclockremastered.notify.admin
```
> [!CAUTION]
> All others can be taken from the Plugin.yml or use LuckPerms, which are automatically suggested there

## Commmands
- /arcm reload
  - Reloads the config
- /arcm help
  - Shows all commands and descriptions
- /arcm display
  - Shows current cached redstone clocks

## More information / external links / Download
Hangar: https://hangar.papermc.io/OneLiteFeather/AntiRedstoneClock-Remastered

Modrinth: https://modrinth.com/plugin/AntiRedstoneClock-Remastered

Discord: https://discord.onelitefeather.net

## Release Cycle
**Important Announcement:** While we have already been using Semantic Versioning (SemVer) for our releases, starting from July 24, 2025, we will be implementing the "semantic-release" tool to automate this process.

### What is Semantic Versioning?
Semantic Versioning follows the format of **MAJOR.MINOR.PATCH** (e.g., 2.4.1):

- **MAJOR** version increases when incompatible API changes are made
- **MINOR** version increases when functionality is added in a backward-compatible manner
- **PATCH** version increases when backward-compatible bug fixes are implemented

### How Commits Affect Version Numbers
Different types of commits will automatically trigger different version increments:

1. **MAJOR version bump** (e.g., 1.2.3 → 2.0.0)
   - Breaking changes to the API
   - Commits with `BREAKING CHANGE:` in the commit message
   - Commits with `!` after the type (e.g., `feat!: remove deprecated methods`)

2. **MINOR version bump** (e.g., 1.2.3 → 1.3.0)
   - Commits with type `feat` (new features)
   - Example: `feat: add new command for clock statistics`

3. **PATCH version bump** (e.g., 1.2.3 → 1.2.4)
   - Commits with type `fix` (bug fixes)
   - Example: `fix: resolve issue with clock detection in nether worlds`

4. **No version bump**
   - Commits with types like `docs`, `style`, `refactor`, `test`, `chore`
   - Example: `docs: update README with new information`

### Example Release Flow
1. Current version: 1.2.3
2. Developer adds a new feature: `feat: add support for custom clock detection rules`
   - Version becomes 1.3.0
3. Developer fixes a bug: `fix: prevent false positives in clock detection`
   - Version becomes 1.3.1
4. Developer makes a breaking change: `feat!: redesign configuration format`
   - Version becomes 2.0.0

### Update Triggers
Please note that version updates can be triggered by:
- **Pull Requests (PRs)**: Any merged PR can trigger a version update based on its commit messages
- **Renovate Bot**: Dependency updates through Renovate will also trigger appropriate version updates

### References
- Official Semantic Versioning specification: https://semver.org/
- Conventional Commits standard: https://www.conventionalcommits.org/
- Semantic Release tool: https://github.com/semantic-release/semantic-release
