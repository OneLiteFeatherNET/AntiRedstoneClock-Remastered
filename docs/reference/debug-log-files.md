# Debug log files

Where the debug output goes and how long it is kept. Switching it on is described in
[Produce a debug log for a bug report](../how-to/produce-a-debug-log-for-a-bug-report.md).

| | |
|---|---|
| Directory | `plugins/AntiRedstoneClock-Remastered/debug-logs/` |
| Current file | `latest.log` |
| Rotated files | `<yyyy-MM-dd>-<n>.log.gz` |
| Rotates | on every server start, and once the file passes 10 MB |
| Retention | `debug.keepFiles` rotated files, default `5` |
| Written by | the server's own Log4j2 |
| Source | `DebugLogging` |

Debug messages go to this file only and never to the server log, because they are far too noisy
for it. Warnings and errors go to both, so the file holds the whole picture when it is attached
to a bug report.

On a server that does not run on Log4j2 the file cannot be written. The server log says so at
the moment debug logging is switched on.
