# AntiRedstoneClock-Remastered
[![Crowdin](https://badges.crowdin.net/e/79ae9c901c3d260349569fca62af7b2f/localized.svg)](https://onelitefeather.crowdin.com/antiredstoneclock-remastered)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/OneLiteFeatherNET/AntiRedstoneClock-Remastered?style=flat-square)](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/releases/latest)
[![GitHub issues](https://img.shields.io/github/issues/OneLiteFeatherNET/AntiRedstoneClock-Remastered?style=flat-square)](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/issues)
[![GitHub license](https://img.shields.io/github/license/OneLiteFeatherNET/AntiRedstoneClock-Remastered?style=flat-square)](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/blob/main/LICENSE)
[![Discord](https://img.shields.io/discord/752527676903784518?style=flat-square)](https://discord.onelitefeather.net)
[![Modrinth](https://img.shields.io/modrinth/dt/AntiRedstoneClock-Remastered?style=flat-square)](https://modrinth.com/plugin/AntiRedstoneClock-Remastered)
![Dependency Track](https://dependency-track.onelitefeather.dev/api/v1/badge/vulns/project/682857a9-0cd2-4ffd-a2b3-098eeba5ab74?style=flat-square)


This plugin is inspired by https://gitlab.com/Trafalcraft/antiRedstoneClock

We re-created the whole code and improved everything. Additionally, we support PlotSquared v7 and WorldGuard v7.

### Goal
The goal of this plugin is to detect redstone clocks, inform staff or console about active ones and optionally destroy / disable the redstone clocks so your server can save performance for something else. It can also prevent players from causing harm with heavy redstone clocks, but this is more of a side effect.

### Not a goal
It's not planned to support Paper forks or Spigot directly - this plugin is developed to work on Paper, if you are using a fork of Paper or Spigot and it doesn't work because of your fork, you're likely on your own.
Also this plugin is not a "performance tool", it won't make your server run better directly.
Support for Minecraft 1.13 and earlier is not planned.

## Minecraft Version Support
Only the last 2 versions of a major Minecraft are supported.
For Minecraft version 1.19, it would be 1.19.4,
For Minecraft version 1.20, it would be 1.20.6 and so on

## Features
- 1.20+ Support
- Java 21 support only
- Plotsquared v6 and v7 support
- WorldGuard Support
- Ready for Minecraft 1.20 and 1.21 ready
- Clock detection
- Sculk support
- Hopper clock detection
- Config Migration (Coming Soon)
- Prevent duplicated loading of anti-redstoneclock plugins

> [!CAUTION]
> The "world" world is ignored by default.

## Contribution
You want to help us? Sure, go for it!
We would love to see your contribution! You can look for open issues or if you have a new idea, please open an issue or ask us on Discord if you can submit your feature with a PR. Communication is a key.

## Dependencies (soft-depend, can be used together)
- PlotSquared v7 https://github.com/IntellectualSites/PlotSquared
- WorldGuard v7 https://github.com/EngineHub/WorldGuard

## Permissions:
```
antiredstoneclockremastered.notify.admin
```
> [!CAUTION]
> All others can be found in plugin.yml or use LuckPerms, which are automatically suggested there

## Commands
- /arcm reload
  - Reloads the config
- /arcm help
  - Shows all commands and descriptions
- /arcm display
  - Display the currently cached redstone clocks

## More information / external links / Download
Hangar: https://hangar.papermc.io/OneLiteFeather/AntiRedstoneClock-Remastered

Modrinth: https://modrinth.com/plugin/AntiRedstoneClock-Remastered

Discord: https://discord.onelitefeather.net

## Release Cycle
**Important Announcement:** We use Semantic Versioning (SemVer) for our releases, automated with [Release Please](https://github.com/googleapis/release-please). Instead of releasing on every push to `main`, Release Please opens and updates a release pull request that collects the pending changelog. Merging that pull request is what bumps the version, writes `CHANGELOG.md`, creates the tag and the GitHub release, and publishes to Hangar and Modrinth.

Because the version and the changelog are derived from commit messages, every pull request title and every commit on the branch is checked against the [Conventional Commits](https://www.conventionalcommits.org/) standard by the `pr-lint` workflow.

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
   - Release Please opens a release pull request for 1.3.0
3. Developer fixes a bug: `fix: prevent false positives in clock detection`
   - The same release pull request is updated and still targets 1.3.0
4. Developer makes a breaking change: `feat!: redesign configuration format`
   - The release pull request now targets 2.0.0
5. A maintainer merges the release pull request
   - Version 2.0.0 is tagged, released and published

### Update Triggers
Please note that version updates can be triggered by:
- **Pull Requests (PRs)**: Any merged PR can trigger a version update based on its commit messages
- **Renovate Bot**: Dependency updates through Renovate will also trigger appropriate version updates

### References
- Official Semantic Versioning specification: https://semver.org/
- Conventional Commits standard: https://www.conventionalcommits.org/
- Release Please: https://github.com/googleapis/release-please
