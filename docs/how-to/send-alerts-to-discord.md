# Send alerts to Discord

Post a message into a Discord channel every time a redstone clock is detected.

**Before you start:** you need permission to create a webhook in the target Discord channel.

1. In Discord, open *Channel Settings → Integrations → Webhooks*, create a webhook and
   copy its URL.
2. Open `plugins/AntiRedstoneClock-Remastered/config.yml` and paste the URL into
   `notification.discord.webhook`.
3. Make sure `discord` is listed under `notification.enabled`.
4. Apply the change:

   ```
   /arcm reload
   ```

## Check it worked

Build a redstone clock somewhere the plugin is watching. An embed appears in the Discord
channel naming the world and the coordinates.

## If it does not work

If the webhook URL cannot be parsed, the plugin **disables itself** on startup and writes
`Failed to create webhook client` to the server log. Check the URL, then restart the server —
a `/arcm reload` cannot bring a disabled plugin back.

If the URL is valid but Discord rejects the message, the server log gets
`Could not send the notification to Discord` and the plugin keeps running.

See also: [Configuration reference](../reference/configuration.md) for the `notification.discord.*` keys,
including how to change the embed colour, description and fields.
