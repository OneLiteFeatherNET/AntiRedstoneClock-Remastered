# AntiRedstoneClock-Remastered
[![Crowdin](https://badges.crowdin.net/e/79ae9c901c3d260349569fca62af7b2f/localized.svg)](https://onelitefeather.crowdin.com/antiredstoneclock-remastered)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/OneLiteFeatherNET/AntiRedstoneClock-Remastered?style=flat-square)](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/releases/latest)
[![GitHub issues](https://img.shields.io/github/issues/OneLiteFeatherNET/AntiRedstoneClock-Remastered?style=flat-square)](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/issues)
[![GitHub license](https://img.shields.io/github/license/OneLiteFeatherNET/AntiRedstoneClock-Remastered?style=flat-square)](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/blob/main/LICENSE)
[![Discord](https://img.shields.io/discord/752527676903784518?style=flat-square)](https://discord.onelitefeather.net)
[![Modrinth](https://img.shields.io/modrinth/dt/AntiRedstoneClock-Remastered?style=flat-square)](https://modrinth.com/plugin/AntiRedstoneClock-Remastered)
![Dependency Track](https://dependency-track.onelitefeather.dev/api/v1/badge/vulns/project/682857a9-0cd2-4ffd-a2b3-098eeba5ab74?style=flat-square)

A Paper plugin that detects running redstone clocks, tells your staff or the console where they
are, and optionally disables or destroys them — so your server spends its time on something
else. It works with PlotSquared and WorldGuard when they are installed, and supports Folia.

## Documentation

**[📖 Read the documentation][docs]** — it has search.

- New to the plugin? [Detect your first redstone clock][tutorial]
- Setting it up? The how-to guides cover [Discord alerts][discord-alerts], [ignoring a
  world][ignore-world], [region exceptions][regions] and [reporting clocks without breaking
  them][report-only].
- Looking up a value? [Configuration][config] · [Commands][commands] · [Permissions][perms]
- Wondering how it decides what a clock is? [How detection works][detection]

The pages are written in [`docs/`](docs/) and published from `main` on every change.

## Download

- [Hangar](https://hangar.papermc.io/OneLiteFeather/AntiRedstoneClock-Remastered)
- [Modrinth](https://modrinth.com/plugin/AntiRedstoneClock-Remastered)

Requires Java 25 and a [supported Minecraft version][versions].

## Get help

- [Open an issue](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/issues) —
  please attach a [debug log][debug-log]
- [Discord](https://discord.onelitefeather.net)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for the build, the commit conventions and how releases
are cut.

This plugin is inspired by [Trafalcraft's antiRedstoneClock](https://gitlab.com/Trafalcraft/antiRedstoneClock);
the code was re-created from scratch. Licensed under the terms in [LICENSE](LICENSE).

[docs]: https://arcr.onelitefeather.net/
[tutorial]: https://arcr.onelitefeather.net/tutorial/detect-your-first-clock
[discord-alerts]: https://arcr.onelitefeather.net/how-to/send-alerts-to-discord
[ignore-world]: https://arcr.onelitefeather.net/how-to/exclude-a-world-from-detection
[regions]: https://arcr.onelitefeather.net/how-to/allow-redstone-clocks-in-a-region
[report-only]: https://arcr.onelitefeather.net/how-to/report-clocks-without-breaking-them
[debug-log]: https://arcr.onelitefeather.net/how-to/produce-a-debug-log-for-a-bug-report
[config]: https://arcr.onelitefeather.net/reference/configuration
[commands]: https://arcr.onelitefeather.net/reference/commands
[perms]: https://arcr.onelitefeather.net/reference/permissions
[versions]: https://arcr.onelitefeather.net/reference/supported-versions
[detection]: https://arcr.onelitefeather.net/explanation/how-detection-works
