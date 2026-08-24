# Stop warning signs from being placed

Out of the box the plugin replaces the block of a detected clock with a sign carrying a
warning text. Turn that off and leave the world untouched.

**Before you start:** none.

1. Open `plugins/AntiRedstoneClock-Remastered/config.yml`.
2. Remove the line `- sign` from the `notification.enabled` list.
3. Apply the change:

   ```
   /arcm reload
   ```

## Check it worked

Build a redstone clock in a watched world. The notification arrives and no sign is left behind.

## If it does not work

There is no command for this target — it is only configurable in the file. If signs still
appear, the file was not saved before `/arcm reload` ran, or a second copy of the plugin is
installed.

See also: [Configuration reference](../reference/configuration.md) for `notification.sign.*` ·
[What happens to a detected clock](../explanation/what-happens-to-a-detected-clock.md)
