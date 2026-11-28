# What happens to a clock once it is found?

Detection and reaction are two separate things, and the reaction is where most surprises on a
fresh installation come from. Out of the box the plugin does not only report a clock: it also
places a warning sign, breaks the block and drops it.

## The order of events

When a block reaches the trigger limit, the plugin first sends every enabled notification, and
only then decides what to do with the block.

Notifications go out in a fixed chain — Discord, sign, console, in-game admins — and every
target that is switched on gets its message. The sign target is unlike the other three: it does
not send a message anywhere, it **places a sign** carrying the configured warning text where
the clock's block was — but only once that position is empty. It never replaces a block that is
still there.

After the notifications, the block is dealt with according to two switches. With
`clock.autoBreak` off, nothing happens to the world and the clock keeps running. With it on,
the block is removed — and `clock.drop` decides whether its items drop on the floor first, as
if it had been mined with a silk touch pickaxe.

## What this means in practice

The default configuration has both the sign target and automatic breaking enabled: the clock's
block is removed first, and the sign then takes its empty place. With automatic breaking off
the block stays, so no sign is placed either.

Whichever behaviour you want, pick one of the two:

- Leave the world alone entirely — [Report clocks without breaking them](../how-to/report-clocks-without-breaking-them.md).
- Keep automatic breaking, but stop the signs — [Stop warning signs from being placed](../how-to/stop-warning-signs-from-being-placed.md).

A sign the plugin placed is marked, and breaking it never drops a sign item. That is deliberate:
the sign is a marker the plugin put into the world, not loot.

## What this is not

Nothing is undone. The plugin keeps no record of what a removed block was, and there is no
command to restore it. On a survival server that is an argument for switching automatic
breaking off and treating the notification as a report for a human to act on.

Related: [How detection works](how-detection-works.md) ·
[Configuration reference](../reference/configuration.md)
