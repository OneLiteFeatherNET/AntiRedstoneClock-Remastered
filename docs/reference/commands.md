# Commands

Every command the plugin registers. All of them are reachable as `/arcm`.

`Permission` names the permission string the command checks. Where that string is not declared
in the plugin's own permission list it is marked *undeclared*: it works for server operators but
is not suggested by permission plugins. See [Permissions](permissions.md).

Toggle commands flip the value they name, save `config.yml` and reload the configuration. They
do not take an argument, and the reply states whether the feature ended up enabled or disabled.

### `/arcm help [query]`

| | |
|---|---|
| Arguments | `query` — optional, filters the listing |
| Permission | `antiredstoneclockremastered.command.help` |
| Source | `CommandFrameworkModule` |

Lists the commands and their descriptions.

### `/arcm reload`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.reload` |
| Source | `ReloadCommand` |

Re-reads `config.yml` and applies debug logging. Values marked *restart* in the
[configuration reference](configuration.md) are not affected.

### `/arcm display [page]`

| | |
|---|---|
| Arguments | `page` — optional, defaults to the first page |
| Permission | `antiredstoneclockremastered.command.display` |
| Source | `DisplayActiveClocksCommand` |

Pages through the blocks currently under observation, four per page.

### `/arcm feature check observer`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.feature.check.observer` |
| Writes | `check.observer` |
| Source | `FeatureCommand.toggleObserver` |

Toggles detection of observers.

### `/arcm feature check piston`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.feature.check.piston` |
| Writes | `check.piston` |
| Source | `FeatureCommand.togglePiston` |

Toggles detection of pistons.

### `/arcm feature check comparator`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.feature.check.comparator` — *undeclared* |
| Writes | `check.comparator` |
| Source | `FeatureCommand.toggleComparator` |

Toggles detection of comparators.

### `/arcm feature check sculk`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.feature.check.sculk` |
| Writes | `check.sculk` |
| Source | `FeatureCommand.toggleSculk` |

Toggles `check.sculk`. Detection reads `check.sculkSensor`, so this command does not currently
change what is detected — see [`check.sculk`](configuration.md#checksculk).

### `/arcm feature check hopper`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.feature.check.hopper` |
| Writes | `check.hopper` |
| Source | `FeatureCommand.toggleHopper` |

Toggles detection of hopper clocks.

### `/arcm feature check redstone_and_repeater`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.feature.check.redstone_and_repeater` |
| Writes | `check.redstoneAndRepeater` |
| Source | `FeatureCommand.toggleRedstoneAndRepeater` |

Toggles detection of redstone dust and repeaters.

### `/arcm feature check ignored_worlds add <world>`

| | |
|---|---|
| Arguments | `world` — a world the server has loaded |
| Permission | `antiredstoneclockremastered.command.feature.check.world.add` |
| Writes | `check.ignoredWorlds` |
| Source | `FeatureCommand.addIgnoredWorld` |

Puts a world on the ignore list.

### `/arcm feature check ignored_worlds remove <world>`

| | |
|---|---|
| Arguments | `world` — a world the server has loaded |
| Permission | `antiredstoneclockremastered.command.feature.check.world.remove` |
| Writes | `check.ignoredWorlds` |
| Source | `FeatureCommand.removeIgnoredWorld` |

Takes a world off the ignore list.

### `/arcm feature check ignored_regions add <region>`

| | |
|---|---|
| Arguments | `region` — a WorldGuard region id |
| Permission | `antiredstoneclockremastered.command.feature.check.region.add` |
| Writes | `check.ignoredRegions` |
| Source | `FeatureCommand.addIgnoredRegion` |

Puts a WorldGuard region on the ignore list. The id is not validated.

### `/arcm feature check ignored_regions remove <region>`

| | |
|---|---|
| Arguments | `region` — a WorldGuard region id |
| Permission | `antiredstoneclockremastered.command.feature.check.region.remove` |
| Writes | `check.ignoredRegions` |
| Source | `FeatureCommand.removeIgnoredRegion` |

Takes a WorldGuard region off the ignore list.

### `/arcm feature clock autoBreak`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.feature.clock.autoBreak` — *undeclared* |
| Writes | `clock.autoBreak` |
| Source | `FeatureCommand.toggleAutoBreak` |

Toggles whether a detected clock's block is removed.

### `/arcm feature clock notify_admins`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.feature.clock.notifyAdmins` |
| Writes | `clock.notifyAdmins` |
| Source | `FeatureCommand.toggleNotifyAdmins` |

Toggles the in-game notification. Listing `admins` under `notification.enabled` enables it
regardless of this value.

### `/arcm feature clock notify_console`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.feature.clock.notifyConsole` |
| Writes | `clock.notifyConsole` |
| Source | `FeatureCommand.toggleNotifyConsole` |

Toggles the console notification. Listing `console` under `notification.enabled` enables it
regardless of this value.

### `/arcm feature clock drop`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.feature.clock.drop` |
| Writes | `clock.drop` |
| Source | `FeatureCommand.toggleDrop` |

Toggles whether a removed block drops its items.

### `/arcm feature clock endDelay <delay>`

| | |
|---|---|
| Arguments | `delay` — integer, seconds |
| Permission | `antiredstoneclockremastered.command.feature.clock.enddelay` |
| Writes | `clock.endDelay` |
| Source | `FeatureCommand.setEndDelay` |

Sets how long a block stays under observation.

### `/arcm feature clock maxCount <count>`

| | |
|---|---|
| Arguments | `count` — integer, triggers |
| Permission | `antiredstoneclockremastered.command.feature.clock.maxCount` |
| Writes | `clock.maxCount` |
| Source | `FeatureCommand.setMaxCount` |

Sets the trigger limit at which a block counts as a clock.

### `/arcm feature debug`

| | |
|---|---|
| Arguments | none |
| Permission | `antiredstoneclockremastered.command.feature.debug` |
| Writes | `debug.enabled` |
| Source | `FeatureCommand.toggleDebug` |

Toggles debug logging. Takes effect immediately.
