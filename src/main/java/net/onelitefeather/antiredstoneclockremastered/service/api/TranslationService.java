package net.onelitefeather.antiredstoneclockremastered.service.api;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

public interface TranslationService {

    void registerAll(final @NotNull Locale locale, final @NotNull ResourceBundle bundle, final boolean escapeSingleQuotes);

    void registerGlobal();

}
