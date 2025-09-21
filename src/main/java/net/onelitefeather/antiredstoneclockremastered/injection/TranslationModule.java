package net.onelitefeather.antiredstoneclockremastered.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered;
import net.onelitefeather.antiredstoneclockremastered.service.api.TranslationService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.LegacyTranslationService;
import net.onelitefeather.antiredstoneclockremastered.service.impl.ModernTranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;

public final class TranslationModule extends AbstractModule {

    private static final String RESOURCE_BUNDLE_NAME = "antiredstoneclockremasterd";
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationModule.class);

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public TranslationService provideTranslationService() {
        ServerBuildInfo buildInfo = ServerBuildInfo.buildInfo();
        if (buildInfo.minecraftVersionId().startsWith("1.20")) {
            LOGGER.info("Using legacy translation service");
            return new LegacyTranslationService();
        } else {
            LOGGER.info("Using modern translation service");
            return new ModernTranslationService();
        }
    }

    @Inject
    public void bootstrap(AntiRedstoneClockRemastered plugin, TranslationService translationService) {
        // Setup translations
        Path langFolder = plugin.getDataFolder().toPath().resolve("lang");
        if (Files.notExists(langFolder)) {
            try {
                Files.createDirectories(langFolder);
            } catch (IOException e) {
                LOGGER.error("An error occurred while creating lang folder");
                return;
            }
        }
        var languages = new HashSet<>(plugin.getConfig().getStringList("translations"));
        languages.add("en-US");
        languages.stream()
                .map(Locale::forLanguageTag)
                .forEach(locale -> loadAndRegisterTranslation(locale, langFolder, translationService));
        translationService.registerGlobal();
    }

    private void loadAndRegisterTranslation(Locale locale, Path langFolder, TranslationService translationService) {
        try {
            ResourceBundle bundle = loadResourceBundle(locale, langFolder);
            if (bundle != null) {
                translationService.registerAll(locale, bundle, false);
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred while loading language file for locale {}", locale, e);
        }
    }

    private ResourceBundle loadResourceBundle(Locale locale, Path langFolder) throws Exception {
        Path langFile = langFolder.resolve(RESOURCE_BUNDLE_NAME + "_" + locale.toLanguageTag() + ".properties");

        if (Files.exists(langFile)) {
            try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{langFolder.toUri().toURL()})) {
                return ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale, urlClassLoader, UTF8ResourceBundleControl.get());
            }
        } else {
            return ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale, UTF8ResourceBundleControl.get());
        }
    }
}
