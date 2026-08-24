# Tune how quickly a clock is detected

Raise the threshold so that a fast farm is left alone, or lower it so that a slow clock is
still caught.

**Before you start:** none.

1. Decide how many triggers a block may produce before it counts as a clock, and set it:

   ```
   /arcm feature clock maxCount 400
   ```

2. Decide how long a block stays under observation before its counter is forgotten, in
   seconds, and set it:

   ```
   /arcm feature clock endDelay 120
   ```

Both commands save and reload the configuration themselves.

A block is reported when it reaches `clock.maxCount` triggers **within** `clock.endDelay`
seconds. Raising `maxCount` or lowering `endDelay` makes detection less eager; doing the
opposite makes it catch slower clocks.

## Check it worked

Run the farm that was being caught. With a high enough `maxCount` it now keeps running, while
an observer clock — which triggers far faster — is still detected within seconds.

## If it does not work

Blocks already under observation keep the counter they had. Break and replace the farm's
trigger block, or restart the server, to measure again from zero.

See also: [Configuration reference](../reference/configuration.md) for `clock.maxCount` and
`clock.endDelay` · [How detection works](../explanation/how-detection-works.md)
