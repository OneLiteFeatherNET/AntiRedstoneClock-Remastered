# What this plugin is for, and what it is not

The plugin detects redstone clocks, tells staff or the console about them, and can disable or
destroy them, so the server spends its time on something else. Preventing players from doing
harm with heavy redstone is a welcome side effect rather than the point.

It is a re-creation of [Trafalcraft's antiRedstoneClock](https://gitlab.com/Trafalcraft/antiRedstoneClock),
rewritten from scratch with support for current PlotSquared and WorldGuard versions.

## This is not a performance tool

Removing a clock frees whatever that clock was costing, and nothing else. The plugin will not
make a server run better on its own, and a server with performance problems that are not caused
by redstone will have exactly the same problems afterwards. It also does its own work only
while the server is keeping up — when TPS leaves the configured range, detection pauses rather
than adding to the load.

## Paper, and Folia

The plugin is developed against Paper. Folia is supported explicitly: the plugin declares Folia
support and ships region-aware schedulers for it, and the build has dedicated Folia run tasks.

Spigot, CraftBukkit, hybrid servers and Paper forks are not supported. They may well work — but
if something breaks specifically because of a fork, that is not a case the project takes on. The
same applies to Minecraft 1.13 and earlier: not supported, not planned.

The authoritative list of versions the project builds and tests against is in
[Supported versions](../reference/supported-versions.md). The plugin declares an API version of
1.19, so it may load on servers outside that list — untested and unsupported.

## One clock detector per server

Running a second anti-redstone-clock plugin alongside this one is not a supported setup. Two
plugins counting the same events and both breaking blocks produce results neither of them
intends, and neither can be diagnosed from the other's log.

Related: [How detection works](how-detection-works.md) ·
[Report an issue](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/issues)
