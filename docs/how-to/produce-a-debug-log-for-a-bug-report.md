# Produce a debug log for a bug report

Collect the detailed log a maintainer needs when the plugin does not behave as expected.

**Before you start:** none.

1. Switch debug logging on:

   ```
   /arcm feature debug
   ```

   It takes effect immediately, no restart needed.
2. Reproduce the problem.
3. Switch debug logging off again with the same command, so the file stops growing.
4. Attach `plugins/AntiRedstoneClock-Remastered/debug-logs/latest.log` to your report at
   <https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/issues>.

## Check it worked

`latest.log` exists and contains lines mentioning the block type you were testing with.
Debug messages never appear in the server log, so an empty server log means nothing here.

## If it does not work

If the server log says `This server does not run on Log4j2, debug logging is not available`,
the debug file cannot be written on that server software. Attach the server log instead and
say which server software you run.

See also: [Debug log files](../reference/debug-log-files.md) for the path, rotation and retention.
