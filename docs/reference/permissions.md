# Permissions

Generated from the `paper { permissions { ... } }` block in `build.gradle.kts` by
`./gradlew generateReferenceDocs`. Do not edit by hand.

These are the permissions the plugin declares. LuckPerms and comparable plugins read
them from the JAR and suggest them automatically. A permission the plugin checks but
does not declare is not listed here; those are named per command in
[Commands](commands.md).

A permission without a stated default is held by server operators only.

### `antiredstoneclockremastered.bundle.admin`

| | |
|---|---|
| Default | op |
| Grants | 21 child permissions |
| Source | `build.gradle.kts`, `paper.permissions` |

All permissions for AntiRedstoneClock-Remastered

- `antiredstoneclockremastered.command.display`
- `antiredstoneclockremastered.command.feature.check.hopper`
- `antiredstoneclockremastered.command.feature.check.observer`
- `antiredstoneclockremastered.command.feature.check.piston`
- `antiredstoneclockremastered.command.feature.check.redstone_and_repeater`
- `antiredstoneclockremastered.command.feature.check.region.add`
- `antiredstoneclockremastered.command.feature.check.region.remove`
- `antiredstoneclockremastered.command.feature.check.sculk`
- `antiredstoneclockremastered.command.feature.check.world.add`
- `antiredstoneclockremastered.command.feature.check.world.remove`
- `antiredstoneclockremastered.command.feature.clock.drop`
- `antiredstoneclockremastered.command.feature.clock.enddelay`
- `antiredstoneclockremastered.command.feature.clock.maxCount`
- `antiredstoneclockremastered.command.feature.clock.notifyAdmins`
- `antiredstoneclockremastered.command.feature.clock.notifyConsole`
- `antiredstoneclockremastered.command.feature.debug`
- `antiredstoneclockremastered.command.help`
- `antiredstoneclockremastered.command.reload`
- `antiredstoneclockremastered.notify.admin`
- `antiredstoneclockremastered.notify.admin.update`
- `antiredstoneclockremastered.notify.disable.donation`

### `antiredstoneclockremastered.bundle.developers`

| | |
|---|---|
| Default | op |
| Grants | 3 child permissions |
| Source | `build.gradle.kts`, `paper.permissions` |

Permissions for developers of AntiRedstoneClock-Remastered

- `antiredstoneclockremastered.command.display`
- `antiredstoneclockremastered.command.help`
- `antiredstoneclockremastered.command.reload`

### `antiredstoneclockremastered.command.display`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Use /arcm display to page through the redstone clocks currently under

### `antiredstoneclockremastered.command.feature.check.hopper`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Toggle detection of hopper clocks with /arcm feature check hopper.

### `antiredstoneclockremastered.command.feature.check.observer`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Toggle detection of observers with /arcm feature check observer.

### `antiredstoneclockremastered.command.feature.check.piston`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Toggle detection of pistons with /arcm feature check piston.

### `antiredstoneclockremastered.command.feature.check.redstone_and_repeater`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Toggle detection of redstone dust and repeaters with /arcm feature

### `antiredstoneclockremastered.command.feature.check.region.add`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Put a WorldGuard region on the ignore list with /arcm feature check

### `antiredstoneclockremastered.command.feature.check.region.remove`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Take a WorldGuard region off the ignore list with /arcm feature check

### `antiredstoneclockremastered.command.feature.check.sculk`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Toggle detection of sculk sensors with /arcm feature check sculk.

### `antiredstoneclockremastered.command.feature.check.world.add`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Put a world on the ignore list with /arcm feature check ignored_worlds

### `antiredstoneclockremastered.command.feature.check.world.remove`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Take a world off the ignore list with /arcm feature check ignored_worlds

### `antiredstoneclockremastered.command.feature.clock.drop`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Toggle whether a removed clock drops its items with /arcm feature

### `antiredstoneclockremastered.command.feature.clock.enddelay`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Set how long a block stays under observation with /arcm feature clock

### `antiredstoneclockremastered.command.feature.clock.maxCount`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Set the trigger limit with /arcm feature clock maxCount <count>.

### `antiredstoneclockremastered.command.feature.clock.notifyAdmins`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Toggle the in-game admin notification with /arcm feature clock notify_admins.

### `antiredstoneclockremastered.command.feature.clock.notifyConsole`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Toggle the console notification with /arcm feature clock notify_console.

### `antiredstoneclockremastered.command.feature.debug`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Toggle debug logging with /arcm feature debug.

### `antiredstoneclockremastered.command.help`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Use /arcm help to list the commands and their descriptions.

### `antiredstoneclockremastered.command.reload`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Use /arcm reload to re-read config.yml.

### `antiredstoneclockremastered.notify.admin`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Receive the in-game message when a redstone clock is detected. The

### `antiredstoneclockremastered.notify.admin.update`

| | |
|---|---|
| Default | op |
| Source | `build.gradle.kts`, `paper.permissions` |

Receive a notice on join when a newer plugin version is available

### `antiredstoneclockremastered.notify.disable.donation`

| | |
|---|---|
| Default | not declared, operators only |
| Source | `build.gradle.kts`, `paper.permissions` |

Suppress the donation notice that is shown on join.

