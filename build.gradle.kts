import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import xyz.jpenilla.runpaper.task.RunServer
import xyz.jpenilla.runtask.pluginsapi.PluginDownloadService
import xyz.jpenilla.runtask.service.DownloadsAPIService

plugins {
    id("java")
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.paper.run)
    alias(libs.plugins.paper.yml)
    alias(libs.plugins.hangar)
    alias(libs.plugins.modrinth)
    alias(libs.plugins.cyclonedx)
    jacoco
}

version = "2.10.0" // x-release-please-version

if (!File("$rootDir/.git").exists()) {
    logger.lifecycle(
        """
    **************************************************************************************
    You need to fork and clone this repository! Don't download a .zip file.
    If you need assistance, consult the GitHub docs: https://docs.github.com/get-started/quickstart/fork-a-repo
    **************************************************************************************
    """.trimIndent()
    ).also { System.exit(1) }
}
val supportedMinecraftVersions = listOf(
    "1.20.6",
    "1.21",
    "1.21.1",
    "1.21.2",
    "1.21.3",
    "1.21.4",
    "1.21.5",
    "1.21.6",
    "1.21.7",
    "1.21.8",
    "1.21.9",
    "1.21.10",
    "1.21.11",
    "26.1",
    "26.1.1",
    "26.1.2",
    "26.2"
)
allprojects {
    apply {
        plugin("java")
    }

    version = rootProject.version

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        if (name != "internal-api") {
            compileOnly(project(":internal-api"))
        }
        compileOnly(rootProject.libs.paper)
    }
}

dependencies {
    constraints {
        // CVE-2023-5072 (HIGH): org.json below 20231013 can be driven into a
        // parser denial of service. It reaches the shaded jar transitively via
        // club.minnced:discord-webhooks, which still resolves 20230618.
        // Found by the Trivy gate in .github/workflows/release-please.yml.
        implementation("org.json:json:20231013") {
            because("CVE-2023-5072 in the version discord-webhooks pulls in")
        }
    }

    implementation(libs.bstats)
    implementation(libs.cloud.command.paper)
    implementation(libs.cloud.command.extras)
    implementation(libs.cloud.command.annotations)
    implementation(libs.semver)
    implementation(libs.adventure.text.discord)
    implementation(libs.jda.webhook)
    implementation(libs.customblockdata)
    implementation(libs.guice)
    implementation(libs.jakarta.inject)
    annotationProcessor(libs.cloud.command.annotations)

    implementation(project(":internal-api"))
    implementation(project(":WorldGuardv6Support"))
    implementation(project(":WorldGuardv7Support"))
    implementation(project(":PlotSquaredv6Support"))
    implementation(project(":PlotSquaredv7Support"))

    compileOnly(libs.log4j.core)

    // Testing dependencies
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.log4j.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.mockbukkit)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks {
    named("build") {
        dependsOn(shadowJar)
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = false
        }
        maxParallelForks = 1
        
        // Generate test reports
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
        
        // Test result publication
        finalizedBy(jacocoTestReport)
    }
    supportedMinecraftVersions.forEach { serverVersion ->
        register<RunServer>("run-$serverVersion") {
            minecraftVersion(serverVersion)
            jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
            group = "run paper"
            runDirectory.set(file("run-$serverVersion"))
            pluginJars(rootProject.tasks.shadowJar.map { it.archiveFile }.get())
        }
    }
    supportedMinecraftVersions.forEach { serverVersion ->
        register<RunServer>("run-folia-$serverVersion") {
            minecraftVersion(serverVersion)
            jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
            group = "run folia"
            runDirectory.set(file("run-folia-$serverVersion"))
            pluginJars(rootProject.tasks.shadowJar.map { it.archiveFile }.get())
            downloadsApiService.convention(DownloadsAPIService.folia(project))
            pluginDownloadService.convention(PluginDownloadService.paper(project))
        }
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "net.onelitefeather.antiredstoneclockremastered.org.bstats")
        relocate("com.jeff_media.customblockdata", "net.onelitefeather.antiredstoneclockremastered.com.jeff_media.customblockdata")
        // Pulled in transitively by club.minnced:discord-webhooks -> okhttp -> kotlin-stdlib.
        // An unrelocated kotlin/ package makes eco flag this plugin as conflicting (#216).
        relocate("kotlin", "net.onelitefeather.antiredstoneclockremastered.kotlin")
        relocate("kotlinx", "net.onelitefeather.antiredstoneclockremastered.kotlinx")
        relocate("okhttp3", "net.onelitefeather.antiredstoneclockremastered.okhttp3")
        relocate("okio", "net.onelitefeather.antiredstoneclockremastered.okio")
        dependsOn(jar)
    }
    this.modrinth {
        dependsOn(shadowJar)
    }

    this.publishAllPublicationsToHangar {
        dependsOn(shadowJar)
    }
    
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

runPaper.folia {
    registerTask()
}


paper {
    main = "net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered"
    apiVersion = "1.19"
    authors = listOf("OneLiteFeather", "TheMeinerLP")
    foliaSupported = true
    serverDependencies {
        register("PlotSquared") {
            required = false
        }
        register("WorldGuard") {
            required = false
        }
    }
    permissions {
        register("antiredstoneclockremastered.notify.admin") {
            description = "Receive the in-game message when a redstone clock is detected. The holder must also be a server operator."
        }
        register("antiredstoneclockremastered.notify.disable.donation") {
            description = "Suppress the donation notice that is shown on join."
        }
        register("antiredstoneclockremastered.notify.admin.update") {
            description = "Receive a notice on join when a newer plugin version is available on Hangar."
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("antiredstoneclockremastered.command.reload") {
            description = "Use /arcm reload to re-read config.yml."
        }
        register("antiredstoneclockremastered.command.help") {
            description = "Use /arcm help to list the commands and their descriptions."
        }
        register("antiredstoneclockremastered.command.display") {
            description = "Use /arcm display to page through the redstone clocks currently under observation."
        }
        register("antiredstoneclockremastered.command.feature.check.observer") {
            description = "Toggle detection of observers with /arcm feature check observer."
        }
        register("antiredstoneclockremastered.command.feature.check.piston") {
            description = "Toggle detection of pistons with /arcm feature check piston."
        }
        register("antiredstoneclockremastered.command.feature.check.sculk") {
            description = "Toggle detection of sculk sensors with /arcm feature check sculk."
        }
        register("antiredstoneclockremastered.command.feature.check.redstone_and_repeater") {
            description = "Toggle detection of redstone dust and repeaters with /arcm feature check redstone_and_repeater."
        }
        register("antiredstoneclockremastered.command.feature.check.hopper") {
            description = "Toggle detection of hopper clocks with /arcm feature check hopper."
        }
        register("antiredstoneclockremastered.command.feature.check.world.add") {
            description = "Put a world on the ignore list with /arcm feature check ignored_worlds add <world>."
        }
        register("antiredstoneclockremastered.command.feature.check.world.remove") {
            description = "Take a world off the ignore list with /arcm feature check ignored_worlds remove <world>."
        }
        register("antiredstoneclockremastered.command.feature.check.region.remove") {
            description = "Take a WorldGuard region off the ignore list with /arcm feature check ignored_regions remove <region>."
        }
        register("antiredstoneclockremastered.command.feature.check.region.add") {
            description = "Put a WorldGuard region on the ignore list with /arcm feature check ignored_regions add <region>."
        }
        register("antiredstoneclockremastered.command.feature.clock.notifyAdmins") {
            description = "Toggle the in-game admin notification with /arcm feature clock notify_admins."
        }
        register("antiredstoneclockremastered.command.feature.clock.notifyConsole") {
            description = "Toggle the console notification with /arcm feature clock notify_console."
        }
        register("antiredstoneclockremastered.command.feature.clock.drop") {
            description = "Toggle whether a removed clock drops its items with /arcm feature clock drop."
        }
        register("antiredstoneclockremastered.command.feature.clock.enddelay") {
            description = "Set how long a block stays under observation with /arcm feature clock endDelay <delay>."
        }
        register("antiredstoneclockremastered.command.feature.clock.maxCount") {
            description = "Set the trigger limit with /arcm feature clock maxCount <count>."
        }
        register("antiredstoneclockremastered.command.feature.debug") {
            description = "Toggle debug logging with /arcm feature debug."
        }
        register("antiredstoneclockremastered.bundle.admin") {
            children = listOf(
                "antiredstoneclockremastered.notify.admin",
                "antiredstoneclockremastered.notify.disable.donation",
                "antiredstoneclockremastered.notify.admin.update",
                "antiredstoneclockremastered.command.reload",
                "antiredstoneclockremastered.command.help",
                "antiredstoneclockremastered.command.feature.check.observer",
                "antiredstoneclockremastered.command.feature.check.piston",
                "antiredstoneclockremastered.command.feature.check.sculk",
                "antiredstoneclockremastered.command.feature.check.redstone_and_repeater",
                "antiredstoneclockremastered.command.feature.check.hopper",
                "antiredstoneclockremastered.command.feature.check.world.add",
                "antiredstoneclockremastered.command.feature.check.world.remove",
                "antiredstoneclockremastered.command.feature.check.region.remove",
                "antiredstoneclockremastered.command.feature.check.region.add",
                "antiredstoneclockremastered.command.feature.clock.notifyAdmins",
                "antiredstoneclockremastered.command.feature.clock.notifyConsole",
                "antiredstoneclockremastered.command.feature.clock.drop",
                "antiredstoneclockremastered.command.feature.clock.enddelay",
                "antiredstoneclockremastered.command.feature.clock.maxCount",
                "antiredstoneclockremastered.command.feature.debug",
                "antiredstoneclockremastered.command.display"
            )
            default = BukkitPluginDescription.Permission.Default.OP
            description = "All permissions for AntiRedstoneClock-Remastered"
        }

        register("antiredstoneclockremastered.bundle.developers") {
            children = listOf(
                "antiredstoneclockremastered.command.reload",
                "antiredstoneclockremastered.command.help",
                "antiredstoneclockremastered.command.display"
            )
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Permissions for developers of AntiRedstoneClock-Remastered"
        }
    }
}
val baseVersion = version as String
val baseChannel = with(baseVersion) {
    when {
        contains("SNAPSHOT", true) -> "Snapshot"
        contains("ALPHA", true) -> "Alpha"
        contains("BETA", true) -> "Beta"
        else -> "Release"
    }
}
val changelogContent = "See [GitHub](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/releases/tag/$baseVersion) for release notes."
hangarPublish {
    publications.register("AntiRedstoneClock-Remastered") {
        version.set(baseVersion)
        channel.set(baseChannel)
        changelog.set(changelogContent)
        apiKey.set(System.getenv("HANGAR_SECRET"))
        id.set("AntiRedstoneClock-Remastered")

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set(supportedMinecraftVersions)
            }
        }
    }
}
modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("UWh9tyEa")
    versionType.set(baseChannel.lowercase())
    versionNumber.set(baseVersion)
    versionName.set(baseVersion)
    changelog.set(changelogContent)
    changelog.set(changelogContent)
    uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    gameVersions.addAll(supportedMinecraftVersions)
    loaders.add("paper")
    loaders.add("bukkit")
    loaders.add("folia")
}
