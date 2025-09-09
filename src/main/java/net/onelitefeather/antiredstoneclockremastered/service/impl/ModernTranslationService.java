package net.onelitefeather.antiredstoneclockremastered.service.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.onelitefeather.antiredstoneclockremastered.service.api.TranslationService;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

public final class ModernTranslationService implements TranslationService {

    private final MiniMessageTranslationStore miniMessageTranslationStore;

    public ModernTranslationService() {
        this.miniMessageTranslationStore = MiniMessageTranslationStore.create(Key.key("antiredstoneclockremastered", "translations"));
        this.miniMessageTranslationStore.defaultLocale(Locale.US);
    }

    @Override
    public void registerAll(@NotNull Locale locale, @NotNull ResourceBundle bundle, boolean escapeSingleQuotes) {
        this.miniMessageTranslationStore.registerAll(locale, bundle, escapeSingleQuotes);
    }

    @Override
    public void registerGlobal() {
        GlobalTranslator.translator().addSource(this.miniMessageTranslationStore);
    }
}
