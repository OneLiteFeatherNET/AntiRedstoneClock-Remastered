# Detect your first redstone clock

By the end of this you will have watched AntiRedstoneClock-Remastered find a running
redstone clock, break it and tell you where it was.

You need a Paper server on a [supported Minecraft version](../reference/supported-versions.md),
the plugin JAR, and operator rights on that server. This takes about 10 minutes.

## Install the plugin

1. Stop the server.
2. Copy `AntiRedstoneClock-Remastered.jar` into the `plugins/` directory.
3. Start the server.

The console now prints a line from `[AntiRedstoneClock]` naming the detection mode it
started in, and a new directory `plugins/AntiRedstoneClock-Remastered/` exists with a
`config.yml` inside it.

## Let the plugin watch your world

Out of the box the plugin ignores the world named `world`. Take it off that list:

1. Join the server.
2. Run:

   ```
   /arcm feature check ignored_worlds remove world
   ```

You should now see a confirmation message naming the world you just removed.

## Build a clock

1. Stand on flat ground and place an observer.
2. Place a second observer directly in front of the first one, so that the two faces
   point at each other.

You should now see both observers flashing red, and hear them ticking. That is a
redstone clock: it triggers itself and never stops.

## Watch the plugin react

1. Stand still and wait.

Within a few seconds both observers disappear, a message from `[AntiRedstoneClock]`
arrives in your chat naming the coordinates, and the server console prints
`Redstone Clock detected at: X,Y,Z(...)`.

The chat message is clickable: clicking it teleports you to the spot where the clock was.

## What you did

You installed the plugin, took a world off the ignore list, and saw the full detection
cycle: the plugin counted the triggers of a running clock, broke the blocks that caused
it, and reported the location to you and to the console.

Next: [Report clocks without breaking them](../how-to/report-clocks-without-breaking-them.md)
if you would rather be told about clocks than have them removed automatically.
