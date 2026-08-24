# Notify staff in game

Send a clickable chat message to your staff whenever a clock is detected.

**Before you start:** none.

1. Grant your staff group the permission `antiredstoneclockremastered.notify.admin`.
2. Give the same people operator rights on the server.
3. Make sure `admins` is listed under `notification.enabled` in `config.yml`.
4. Apply the change:

   ```
   /arcm reload
   ```

## Check it worked

Build a redstone clock in a watched world. Staff members who are online receive a message
from `[AntiRedstoneClock]` naming the coordinates. Clicking the message teleports them there.

## If it does not work

The in-game notification requires **both** the permission and operator status. A staff member
who holds `antiredstoneclockremastered.notify.admin` but is not an operator receives nothing.

See also: [Permissions reference](../reference/permissions.md) ·
[What happens to a detected clock](../explanation/what-happens-to-a-detected-clock.md)
