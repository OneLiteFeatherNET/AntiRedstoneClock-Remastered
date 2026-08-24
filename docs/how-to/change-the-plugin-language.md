# Change the plugin language

Make the plugin speak your players' language instead of English.

**Before you start:** none.

1. Open `plugins/AntiRedstoneClock-Remastered/config.yml`.
2. Add the language tags you want to the `translations` list, for example:

   ```yaml
   translations:
     - 'en-US'
     - 'de-DE'
   ```

3. Restart the server. Translations are loaded once at startup — `/arcm reload` does not
   pick up a new language.

Each player then sees the messages in the language their Minecraft client is set to, as long
as that language is in the list. Everyone else falls back to `en-US`, which is always loaded.

## Check it worked

Set your client to the language you added, rejoin, and run `/arcm help`. The command
descriptions appear in that language.

## If it does not work

The server log names every locale it loaded on startup. A tag that does not match a shipped
file is reported there as an error.

To change the wording, or to add a language that is not shipped, put your own
`antiredstoneclockremasterd_<tag>.properties` into
`plugins/AntiRedstoneClock-Remastered/lang/`. A file in that folder wins over the one inside
the JAR.

See also: [Configuration reference](../reference/configuration.md) for the list of shipped languages.
