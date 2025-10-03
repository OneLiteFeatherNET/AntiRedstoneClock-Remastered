package net.onelitefeather.antiredstoneclockremastered.service.api;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Service for managing translations and localization.
 *
 * @since 2.2.0
 * @version 1.0.0
 * @author TheMeinerLP
 */
public interface TranslationService {

    /**
     * Registers all translations from the given resource bundle for the specified locale.
     *
     * @param locale             The locale for which to register translations.
     * @param bundle             The resource bundle containing translations.
     * @param escapeSingleQuotes Whether to escape single quotes in the translations.
     */
    void registerAll(final Locale locale, final ResourceBundle bundle, final boolean escapeSingleQuotes);

    /**
     * Registers global translations that are not locale-specific.
     */
    void registerGlobal();

}
