# Why are there two tracking modes?

`check.mode` accepts `static` and `dynamic`. Both count triggers the same way; they differ in
how they remember *which* block they are counting.

## Static: remember the position

Static tracking keys its counters by block position. A block at those coordinates triggers,
the counter at those coordinates goes up. This is simple, costs nothing beyond a map in
memory, and is forgotten when the server stops.

Its blind spot is a clock that moves. A flying-machine style construction, or any build where
the triggering component is pushed along, presents a new position on every cycle, and every
position starts its own counter from zero. Such a build never reaches the limit anywhere.

## Dynamic: remember the block

Dynamic tracking gives the observed block an identifier and stores it *in the block itself*,
using the world's block data. The counter follows the block when its position changes, so a
moving construction accumulates triggers across the whole path it travels.

The price is that the identifier is written into the world. It survives a restart, and it stays
on the block until the block is broken.

## What this means in practice

Static is the cheaper and more predictable of the two, and it is what the plugin falls back to
whenever `check.mode` cannot be read — including when the value is misspelled. If the plugin
seems to be running in a different mode than you configured, check the spelling: an unknown
value does not raise an error, it silently selects static.

The mode the plugin actually started in is printed to the console on every startup.

## What this is not

The mode is not a strictness setting. Neither mode detects more clocks than the other on a
stationary build, and switching modes is not a way to make detection more or less eager — for
that, see [Tune how quickly a clock is detected](../how-to/tune-how-quickly-a-clock-is-detected.md).

Related: [How detection works](how-detection-works.md) ·
[Configuration reference](../reference/configuration.md)
