# Allow redstone clocks in a region

Carve out a single area — a farm, a shop district, one plot — where clocks are permitted
while the rest of the world stays protected.

**Before you start:** WorldGuard or PlotSquared has to be installed. The plugin picks up
either one automatically when it is present.

## With WorldGuard

1. Set the region flag:

   ```
   /rg flag <region> redstone-clock allow
   ```

The flag is registered by the plugin at startup and defaults to *deny*.

## With PlotSquared

1. On the plot, set the flag:

   ```
   /plot flag set redstone-clock true
   ```

## Without either plugin

1. Add the region name to `check.ignoredRegions` in `config.yml`, or run:

   ```
   /arcm feature check ignored_regions add <region>
   ```

   This list is only evaluated when WorldGuard is installed — it matches region names, and
   without WorldGuard there are no regions to match.

## Check it worked

Build a clock inside the region and let it run. Nothing is broken and no notification arrives.
Build the same clock just outside the region: it is detected.

## If it does not work

Region membership is checked at the exact block of the triggering component. A clock that
straddles the region border is detected as soon as the triggering block sits outside it.

See also: [How detection works](../explanation/how-detection-works.md) for the order in which
worlds, regions and plots are consulted.
