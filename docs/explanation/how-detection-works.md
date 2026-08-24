# How does the plugin decide that something is a clock?

There is no such thing as a "redstone clock block". A clock is a pattern in time: a component
that keeps triggering itself. The plugin therefore does not look at builds, it counts events.

## Counting triggers

Every relevant block event — a piston firing, a repeater updating, an observer pulsing, a
comparator changing, a sculk sensor reacting, an item completing a round trip between two
hoppers — is offered to the plugin by the server. The first time a block produces such an
event, the plugin starts observing it and gives it a deadline. Every further event from the
same block increments a counter.

A block that reaches the trigger limit before its deadline expires is a clock. A block that
reaches its deadline first is forgotten, and the next event starts a fresh observation. That
is the whole idea: a farm that a player operates now and then never accumulates enough
triggers inside the window, while a self-triggering loop reaches the limit within seconds.

Both numbers are configurable, which is what makes the plugin tunable to a server rather than
to a build. See [Tune how quickly a clock is detected](../how-to/tune-how-quickly-a-clock-is-detected.md).

Hoppers are the exception to the counting rule. Item transfers between hoppers happen
constantly on any server with sorters and storage systems, so counting them would report every
warehouse. Instead the plugin watches the *direction* of transfers within one hopper pair and
only counts a completed back-and-forth cycle, which a sorter never produces and a hopper clock
produces continuously.

## The order of the checks

Before a block is ever counted, an event passes through a chain of filters, each of which can
end the check:

1. **Server load.** If the measured TPS is outside the configured range, the check is skipped
   entirely. This is deliberate: the plugin should not add work while the server is already
   struggling.
2. **Block type.** If detection for that component is switched off, the check ends.
3. **World.** If the world is on the ignore list, the check ends.
4. **WorldGuard.** If a region allows clocks — through the `redstone-clock` flag or through
   the ignore list — the check ends.
5. **PlotSquared.** If the plot allows clocks through its `redstone-clock` flag, the check ends.
6. **Counting.** Only what survives all of the above is counted, and only a block that reaches
   the limit is handed on to be reported and possibly removed.

The order matters for the exceptions you configure. A world on the ignore list is never
consulted against WorldGuard, and a region flag cannot bring back detection in a world that is
ignored — the coarser filter always wins.

## What this means in practice

Two configuration decisions carry almost all of the plugin's behaviour: which components are
watched, and how eager the counter is. Everything else — notifications, breaking, dropping —
happens after the decision has already been made.

## What this is not

This is not a redstone *analyser*. The plugin does not understand what a build does, cannot
tell a clock driving a useful machine from one built to lag the server, and will report both
if they trigger often enough. Deciding which builds are acceptable is what the world, region
and plot exceptions are for.

Related: [What happens to a detected clock](what-happens-to-a-detected-clock.md) ·
[Static and dynamic tracking](static-and-dynamic-tracking.md)
