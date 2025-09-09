package net.onelitefeather.antiredstoneclockremastered.service.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.onelitefeather.antiredstoneclockremastered.service.api.TranslationService;
import net.onelitefeather.antiredstoneclockremastered.translations.PluginTranslationRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

@Deprecated(since = "2.0.13", forRemoval = true)
public final class LegacyTranslationService implements TranslationService {

    private final TranslationRegistry translationRegistry;

    public LegacyTranslationService() {
        this.translationRegistry = new PluginTranslationRegistry(TranslationRegistry.create(Key.key("antiredstoneclockremastered", "translations")));
        this.translationRegistry.defaultLocale(Locale.US);
    }

    @Override
    public void registerAll(@NotNull Locale locale, @NotNull ResourceBundle bundle, boolean escapeSingleQuotes) {
        this.translationRegistry.registerAll(locale, bundle, escapeSingleQuotes);
    }

    @Override
    public void registerGlobal() {
        GlobalTranslator.translator().addSource(this.translationRegistry);
    }
}
