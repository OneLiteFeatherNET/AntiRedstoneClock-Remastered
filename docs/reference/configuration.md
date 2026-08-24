# Configuration

Every key of `plugins/AntiRedstoneClock-Remastered/config.yml`, in the order the file uses.

The authoritative source is `src/main/resources/config.yml` — that is the file the plugin ships
and reads, and this page is maintained alongside it.

`Reload` states how a change takes effect. `/arcm reload` re-reads the file; a value marked
*restart* is only read while the server starts.

## `translations`

### `translations`

| | |
|---|---|
| Type | list of language tags |
| Default | `['en-US']` |
| Allowed values | `af-ZA` `ar-SA` `ca-ES` `cs-CZ` `da-DK` `de-DE` `el-GR` `en-US` `es-ES` `fi-FI` `fr-FR` `he-IL` `hu-HU` `it-IT` `ja-JP` `ko-KR` `nl-NL` `no-NO` `pl-PL` `pt-BR` `pt-PT` `ro-RO` `ru-RU` `sr-Cyrl` `sv-SE` `tr-TR` `uk-UA` `vi-VN` `zh-CN` `zh-TW`, or any tag with a matching file in `lang/` |
| Reload | restart |
| Source | `TranslationModule.bootstrap` |

Languages loaded at startup; each player sees the one their client is set to. `en-US` is always
loaded and is the fallback for everyone else.

```yaml
translations:
  - 'en-US'
  - 'de-DE'
```

## `check`

### `check.mode`

| | |
|---|---|
| Type | string |
| Default | `'dynamic'` |
| Allowed values | `static`, `dynamic` |
| Reload | `/arcm reload` |
| Source | `ConfigMode`, `DelegatedTrackingService` |

Selects how trigger counters are keyed: `static` by block position, `dynamic` by an identifier
stored in the block itself. Any other value, including a misspelling, selects `static` without
an error.

```yaml
check:
  mode: 'static'
```

### `check.redstoneAndRepeater`

| | |
|---|---|
| Type | boolean |
| Default | `true` |
| Reload | `/arcm feature check redstone_and_repeater` |
| Source | `SkipEventTypeRedstoneClockMiddleware` |

Whether redstone dust and repeater updates are counted.

```yaml
check:
  redstoneAndRepeater: true
```

### `check.observer`

| | |
|---|---|
| Type | boolean |
| Default | `true` |
| Reload | `/arcm feature check observer` |
| Source | `SkipEventTypeRedstoneClockMiddleware` |

Whether observer pulses are counted.

```yaml
check:
  observer: true
```

### `check.piston`

| | |
|---|---|
| Type | boolean |
| Default | `true` |
| Reload | `/arcm feature check piston` |
| Source | `SkipEventTypeRedstoneClockMiddleware` |

Whether piston extensions and retractions are counted.

```yaml
check:
  piston: true
```

### `check.comparator`

| | |
|---|---|
| Type | boolean |
| Default | `true` |
| Reload | `/arcm feature check comparator` |
| Source | `SkipEventTypeRedstoneClockMiddleware` |

Whether comparator updates are counted.

```yaml
check:
  comparator: true
```

### `check.sculk`

| | |
|---|---|
| Type | boolean |
| Default | `true` |
| Reload | `/arcm feature check sculk` |
| Source | `SkipEventTypeRedstoneClockMiddleware` |

Intended to switch counting of sculk sensor events on and off. Detection reads the key
`check.sculkSensor`, which the shipped file does not contain, so sculk sensors are currently
never counted whatever this key is set to.

```yaml
check:
  sculk: true
```

### `check.hopper`

| | |
|---|---|
| Type | boolean |
| Default | `true` |
| Reload | `/arcm feature check hopper` |
| Source | `SkipEventTypeRedstoneClockMiddleware`, `HopperClockTracker` |

Whether completed back-and-forth item cycles between two hoppers are counted. One-directional
transfers, as in a sorter, are never counted.

```yaml
check:
  hopper: true
```

### `check.ignoredWorlds`

| | |
|---|---|
| Type | list of strings, world names |
| Default | `['world']` |
| Reload | `/arcm feature check ignored_worlds add\|remove <world>` |
| Source | `WorldRedstoneClockMiddleware` |

Worlds in which no detection runs at all.

```yaml
check:
  ignoredWorlds:
    - world
    - creative
```

### `check.ignoredRegions`

| | |
|---|---|
| Type | list of strings, WorldGuard region ids |
| Default | `[]` |
| Reload | `/arcm feature check ignored_regions add\|remove <region>` |
| Source | `AbstractWorldGuardSupport`, `WorldGuardModernSupport` |

WorldGuard regions in which no detection runs. Evaluated only when WorldGuard is installed.

```yaml
check:
  ignoredRegions:
    - spawn
```

## `clock`

### `clock.endDelay`

| | |
|---|---|
| Type | integer, seconds |
| Default | `300` |
| Reload | `/arcm feature clock endDelay <delay>` |
| Source | `StaticTrackingService`, `DynamicTrackingService` |

How long a block stays under observation before its trigger counter is discarded.

```yaml
clock:
  endDelay: 300
```

### `clock.maxCount`

| | |
|---|---|
| Type | integer, triggers |
| Default | `150` |
| Reload | `/arcm feature clock maxCount <count>` |
| Source | `StaticTrackingService`, `DynamicTrackingService` |

How many triggers a block may produce within `clock.endDelay` before it counts as a clock.

```yaml
clock:
  maxCount: 150
```

### `clock.autoBreak`

| | |
|---|---|
| Type | boolean |
| Default | `true` |
| Reload | `/arcm feature clock autoBreak` |
| Source | `TrackingRedstoneClockMiddleware` |

Whether a detected clock's block is removed. With this off, notifications are still sent and the
world is left untouched.

```yaml
clock:
  autoBreak: true
```

### `clock.notifyAdmins`

| | |
|---|---|
| Type | boolean |
| Default | `false` |
| Reload | `/arcm feature clock notify_admins` |
| Source | `AdminNotificationService` |

Enables the in-game notification. The target is also enabled by listing `admins` under
`notification.enabled`, and either one on its own is enough.

```yaml
clock:
  notifyAdmins: false
```

### `clock.notifyConsole`

| | |
|---|---|
| Type | boolean |
| Default | `true` |
| Reload | `/arcm feature clock notify_console` |
| Source | `ConsoleNotificationService` |

Enables the console notification. The target is also enabled by listing `console` under
`notification.enabled`, and either one on its own is enough.

```yaml
clock:
  notifyConsole: true
```

### `clock.drop`

| | |
|---|---|
| Type | boolean |
| Default | `true` |
| Reload | `/arcm feature clock drop` |
| Source | `TrackingRedstoneClockMiddleware`, `BukkitDecisionService` |

Whether a removed block drops its items, as if mined with a silk touch pickaxe. Has no effect
while `clock.autoBreak` is off.

```yaml
clock:
  drop: true
```

## `debug`

### `debug.enabled`

| | |
|---|---|
| Type | boolean |
| Default | `false` |
| Reload | `/arcm feature debug`, or `/arcm reload` |
| Source | `AntiRedstoneClockRemastered.setupDebugLogging` |

Whether debug messages are written to `debug-logs/latest.log`. Debug messages never appear in
the server log.

```yaml
debug:
  enabled: false
```

### `debug.keepFiles`

| | |
|---|---|
| Type | integer, files |
| Default | `5` |
| Reload | `/arcm reload` |
| Source | `DebugLogging` |

How many rotated debug log files are kept next to `latest.log`.

```yaml
debug:
  keepFiles: 5
```

## `tps`

### `tps.interval`

| | |
|---|---|
| Type | integer, seconds |
| Default | `2` |
| Reload | restart |
| Source | `CheckTPS` |

How often the server tick rate is sampled.

```yaml
tps:
  interval: 2
```

### `tps.max`

| | |
|---|---|
| Type | integer, ticks per second |
| Default | `20` |
| Reload | restart |
| Source | `CheckTPS` |

Upper end of the tick rate at which detection runs. A negative value removes the upper bound.

```yaml
tps:
  max: 20
```

### `tps.min`

| | |
|---|---|
| Type | integer, ticks per second |
| Default | `15` |
| Reload | restart |
| Source | `CheckTPS` |

Lower end of the tick rate at which detection runs; below it, detection pauses. A negative value
removes the lower bound, and detection is switched off entirely when both bounds are negative.

```yaml
tps:
  min: 15
```

## `notification`

### `notification.enabled`

| | |
|---|---|
| Type | list of strings, lowercase |
| Default | `['console', 'admins', 'discord', 'sign']` |
| Allowed values | `console`, `admins`, `discord`, `sign` |
| Reload | `/arcm reload` |
| Source | `ServiceModule.providesNotificationService` |

Which notification targets are active. `sign` does not send a message: it replaces the block of
the detected clock with a sign.

```yaml
notification:
  enabled:
    - console
    - admins
```

### `notification.discord.webhook`

| | |
|---|---|
| Type | string, URL |
| Default | `"_here_comes_the_webhook_url_"` |
| Reload | restart |
| Source | `DiscordNotificationService.createWebHook` |

The Discord webhook the alert is posted to. A value that cannot be parsed as a URL disables the
whole plugin at startup.

```yaml
notification:
  discord:
    webhook: "https://discord.com/api/webhooks/..."
```

### `notification.discord.avatar`

| | |
|---|---|
| Type | string, URL |
| Default | `"https://cdn.modrinth.com/data/UWh9tyEa/ea7b1aded47652100a1f0e8673e87c124b38fc08_96.webp"` |
| Reload | none |
| Source | none |

Not read anywhere. The embed image is taken from `notification.discord.image`, which the shipped
file does not contain, so no image is attached.

```yaml
notification:
  discord:
    avatar: "https://example.invalid/avatar.png"
```

### `notification.discord.color`

| | |
|---|---|
| Type | integer, hex colour without `#` |
| Default | `0xFF00FF` |
| Reload | `/arcm reload` |
| Source | `DiscordNotificationService.sendNotificationMessage` |

Colour of the embed's left border.

```yaml
notification:
  discord:
    color: 0xFF0000
```

### `notification.discord.description`

| | |
|---|---|
| Type | string, MiniMessage |
| Default | `<red><bold>There is a redstone clock in <world> at <x>,<y>,<z></bold>` |
| Allowed placeholders | `<world>`, `<x>`, `<y>`, `<z>` |
| Reload | `/arcm reload` |
| Source | `DiscordNotificationService.getNotificationMessage` |

Body text of the embed.

```yaml
notification:
  discord:
    description: |
      <red>Redstone clock in <world> at <x>,<y>,<z>
```

### `notification.discord.fields`

| | |
|---|---|
| Type | list of `{name, value, inline}` |
| Default | one field, `Teleport Command` |
| Allowed placeholders | `<world>`, `<x>`, `<y>`, `<z>` in `value` |
| Reload | `/arcm reload` |
| Source | `DiscordNotificationService.generateFields` |

Extra fields on the embed. At most five are sent; further entries are ignored.

```yaml
notification:
  discord:
    fields:
      - name: "Teleport Command"
        value: "/teleport <x> <y> <z>"
        inline: true
```

### `notification.sign.material`

| | |
|---|---|
| Type | string, Minecraft material name |
| Default | `OAK_SIGN` |
| Reload | `/arcm reload` |
| Source | `SignNotificationService` |

The block placed where the clock was. An unknown name falls back to `OAK_SIGN` with a warning;
a known material that is not a sign is placed but carries no text.

```yaml
notification:
  sign:
    material: BIRCH_SIGN
```

### `notification.sign.front`

| | |
|---|---|
| Type | list of strings, MiniMessage |
| Default | `['<red>RedstoneClocks!', '<red>Are', '<red>prohibited']` |
| Reload | `/arcm reload` |
| Source | `SignNotificationService` |

Text on the front of the sign. At most 4 lines of at most 16 visible characters; anything beyond
is dropped and reported as a warning in the server log.

```yaml
notification:
  sign:
    front:
      - "<red>No clocks"
```

### `notification.sign.back`

| | |
|---|---|
| Type | list of strings, MiniMessage |
| Default | `['<red>RedstoneClocks!', '<red>Are', '<red>prohibited']` |
| Reload | `/arcm reload` |
| Source | `SignNotificationService` |

Text on the back of the sign, with the same limits as `notification.sign.front`.

```yaml
notification:
  sign:
    back:
      - "<red>No clocks"
```
