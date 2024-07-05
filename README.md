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
Only the last 3 versions of a major Minecraft are supported.
For Minecraft version 1.19, it would be 1.19.4,
For Minecraft version 1.20, it would be 1.20.6 and so on

## Features
- 1.19+ Support
- Java 21 only support
- Plotsquared v6 and v7 support
- WorldGuard Support
- 1.19,1.20,1.21 ready
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
