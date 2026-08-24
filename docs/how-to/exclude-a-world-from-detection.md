# Exclude a world from detection

Stop the plugin from touching redstone in a world — a creative world, a build server, a
technical playground.

**Before you start:** none.

1. Run, with the name of the world:

   ```
   /arcm feature check ignored_worlds add creative
   ```

The command writes the world into `check.ignoredWorlds` and reloads the configuration itself.

To put a world back under observation, use `remove` instead of `add`:

```
/arcm feature check ignored_worlds remove creative
```

## Check it worked

Build a redstone clock in that world and let it run. Nothing is broken and no notification
arrives.

## If it does not work

The world name must be the one the server uses, not the display name in a world-management
plugin. `/arcm feature check ignored_worlds add <world>` only accepts worlds the server has
loaded, so a typo is rejected instead of silently written to the file.

See also: [Configuration reference](../reference/configuration.md) for `check.ignoredWorlds` ·
[How detection works](../explanation/how-detection-works.md)
