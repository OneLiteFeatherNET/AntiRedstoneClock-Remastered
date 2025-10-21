package net.onelitefeather.antiredstoneclockremastered;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.antiredstoneclockremastered.injection.MetricsModule;
import net.onelitefeather.antiredstoneclockremastered.injection.CommandFrameworkModule;
import net.onelitefeather.antiredstoneclockremastered.injection.CommandModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ExternalSupportModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ListenerModule;
import net.onelitefeather.antiredstoneclockremastered.injection.PlatformModule;
import net.onelitefeather.antiredstoneclockremastered.injection.ServiceModule;
import net.onelitefeather.antiredstoneclockremastered.injection.TranslationModule;
import net.onelitefeather.antiredstoneclockremastered.service.UpdateService;
import net.onelitefeather.antiredstoneclockremastered.utils.CheckTPS;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class AntiRedstoneClockRemastered extends JavaPlugin {
    
    // Injector for dependency injection
    private Injector injector;

    public static final Component PREFIX = MiniMessage.miniMessage().deserialize("<gradient:red:white>[AntiRedstoneClock]</gradient>");

    @Override
    public void onLoad() {
        try {
            if (!isJarSigned()) return;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        saveDefaultConfig();
        reloadConfig();
        saveConfig();
        injector = Guice.createInjector(Stage.PRODUCTION, Arrays.asList(
                new PlatformModule(this),
                new TranslationModule(),
                new ServiceModule(),
                new ExternalSupportModule(),
                new CommandModule(),
                new ListenerModule()
                )
        );
    }

    @Override
    public void onEnable() {
        injector.getInstance(TranslationModule.class);
        injector.getInstance(CheckTPS.class).startCheck();
        donationInformation();
        injector.getInstance(UpdateService.class).schedule();
        injector.getInstance(UpdateService.class).run();
        injector.getInstance(UpdateService.class).notifyConsole(getComponentLogger());
        injector.getInstance(CommandFrameworkModule.class).enable();
        injector.getInstance(MetricsModule.class).registerCharts();
        this.injector.getInstance(ListenerModule.class).registerEvents(injector, this);
    }
    @Override
    public void onDisable() {
        injector.getInstance(UpdateService.class).shutdown();
    }

    private void donationInformation() {
        getComponentLogger().info(Component.translatable("antiredstoneclockremastered.notify.donation.console"));
    }

    public static boolean isJarSigned() throws IOException {
        try (JarFile jar = new JarFile(getPlugin(AntiRedstoneClockRemastered.class).getFile())) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return false; // Kein Manifest, daher nicht signiert
            }

            Set<String> signedEntries = manifest.getEntries().keySet();
            for (String entryName : signedEntries) {
                if (entryName.endsWith(".class")) {
                    JarEntry entry = jar.getJarEntry(entryName);
                    if (entry.getCodeSigners() != null && entry.getCodeSigners().length > 0) {
                        return true; // Mindestens eine signierte Klasse gefunden
                    }
                }
            }
            return false; // Keine signierten Klassen gefunden
        }
    }

}
