# AntiRedstoneClock-Remastered
[![Crowdin](https://badges.crowdin.net/e/79ae9c901c3d260349569fca62af7b2f/localized.svg)](https://onelitefeather.crowdin.com/antiredstoneclock-remastered)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/OneLiteFeatherNET/AntiRedstoneClock-Remastered?style=flat-square)](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/releases/latest)
[![GitHub issues](https://img.shields.io/github/issues/OneLiteFeatherNET/AntiRedstoneClock-Remastered?style=flat-square)](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/issues)
[![GitHub license](https://img.shields.io/github/license/OneLiteFeatherNET/AntiRedstoneClock-Remastered?style=flat-square)](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/blob/main/LICENSE)
[![Discord](https://img.shields.io/discord/752527676903784518?style=flat-square)](https://discord.onelitefeather.net)
[![Modrinth](https://img.shields.io/modrinth/dt/AntiRedstoneClock-Remastered?style=flat-square)](https://modrinth.com/plugin/AntiRedstoneClock-Remastered)
![Dependency Track](https://dependency-track.onelitefeather.dev/api/v1/badge/vulns/project/682857a9-0cd2-4ffd-a2b3-098eeba5ab74?style=flat-square)


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

## Features
- 1.20+ Support
- Java 21 only support
- Plotsquared v6 and v7 support
- WorldGuard Support
- 1.20,1.21 ready
- Clock detection
- Sculk support
- Hopper clock detection
- Config Migration(Soon)
- Prevent duplicated loading of anti-redstoneclock plugins

> [!CAUTION]
> The "world" world is ignored by default

## Debug logs
When something does not work as expected, switch on debug logging with

```
/arcm feature debug
```

or in the `config.yml`:

```yaml
debug:
  enabled: true
  # How many rotated log files are kept next to latest.log
  keepFiles: 5
```

Both take effect right away, no restart needed.

Debug messages are written to `plugins/AntiRedstoneClock-Remastered/debug-logs/latest.log` and never
show up in the server log, because they are far too noisy for it. Warnings and errors go to the server
log as usual and are written to this file as well, so it contains the whole picture when you attach it
to a bug report.

The file is handled by the same Log4j2 the server itself logs with, so it behaves just like
`logs/latest.log`: it rolls over on every server start and once it grows past 10 MB, and older files
are compressed to `.log.gz`. `keepFiles` decides how many of them are kept.

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
- /arcm feature debug
  - Turns debug logging on or off
- /arcm display
  - Shows current cached redstone clocks

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
