# Report clocks without breaking them

Let the plugin tell you about clocks and leave the blocks alone — the usual choice on a
survival server where breaking player builds causes more trouble than the clock does.

**Before you start:** none.

1. Run:

   ```
   /arcm feature clock autoBreak
   ```

   The command toggles `clock.autoBreak`, saves the configuration and reloads it.
2. Read the reply: it confirms whether the feature is now enabled or disabled. You want
   **disabled**.

## Check it worked

Build a redstone clock in a watched world. The notification arrives, and the blocks keep
running.

## If it does not work

If the clock is broken anyway, the toggle went the wrong way — the command flips the value
rather than setting it. Run it once more and read the reply.

See also: [Configuration reference](../reference/configuration.md) for `clock.autoBreak` and
`clock.drop` · [What happens to a detected clock](../explanation/what-happens-to-a-detected-clock.md)
