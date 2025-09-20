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
    jacoco
}

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
)
allprojects {
    apply {
        plugin("java")
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
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
    implementation(libs.bstats)
    implementation(libs.cloud.command.paper)
    implementation(libs.cloud.command.extras)
    implementation(libs.cloud.command.annotations)
    implementation(libs.semver)
    implementation(libs.adventure.text.feature.pagination)
    implementation(libs.guice)
    implementation(libs.jakarta.inject)
    annotationProcessor(libs.cloud.command.annotations)

    implementation(project(":internal-api"))
    implementation(project(":WorldGuardv6Support"))
    implementation(project(":WorldGuardv7Support"))
    implementation(project(":PlotSquaredv6Support"))
    implementation(project(":PlotSquaredv7Support"))

    // Testing dependencies
    testImplementation(libs.junit.jupiter)
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
    }
    this.modrinth {
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
        register("antiredstoneclockremastered.notify.admin")
        register("antiredstoneclockremastered.notify.disable.donation")
        register("antiredstoneclockremastered.notify.admin.update") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("antiredstoneclockremastered.command.reload")
        register("antiredstoneclockremastered.command.help")
        register("antiredstoneclockremastered.command.feature.check.observer")
        register("antiredstoneclockremastered.command.feature.check.piston")
        register("antiredstoneclockremastered.command.feature.check.sculk")
        register("antiredstoneclockremastered.command.feature.check.redstone_and_repeater")
        register("antiredstoneclockremastered.command.feature.check.world.add")
        register("antiredstoneclockremastered.command.feature.check.world.remove")
        register("antiredstoneclockremastered.command.feature.check.region.remove")
        register("antiredstoneclockremastered.command.feature.check.region.add")
        register("antiredstoneclockremastered.command.feature.clock.notifyAdmins")
        register("antiredstoneclockremastered.command.feature.clock.notifyConsole")
        register("antiredstoneclockremastered.command.feature.clock.drop")
        register("antiredstoneclockremastered.command.feature.clock.enddelay")
        register("antiredstoneclockremastered.command.feature.clock.maxCount")
    }
}
val baseVersion = version as String
val baseChannel = with(baseVersion) {
    when {
        contains("SNAPSHOT") -> "Snapshot"
        contains("ALPHA") -> "Alpha"
        contains("BETA") -> "Beta"
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
}