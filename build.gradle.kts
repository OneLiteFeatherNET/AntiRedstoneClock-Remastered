import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import xyz.jpenilla.runpaper.task.RunServer

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
    named<Jar>("jar") {
        archiveClassifier.set("unshaded")
    }
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
        // Create version-specific log4j2 configuration
        val createLog4jConfig = register("createLog4jConfig-$serverVersion") {
            doLast {
                val templateFile = file("src/main/resources/log4j2-template.xml")
                val configFile = file("run-$serverVersion/log4j2.xml")
                
                // Ensure the run directory exists
                configFile.parentFile.mkdirs()
                
                // Replace MC_VERSION placeholder with actual version
                val configContent = templateFile.readText()
                    .replace("MC_VERSION", serverVersion)
                    .replace("logs/", "run-$serverVersion/logs/")
                
                configFile.writeText(configContent)
                logger.info("Created log4j2 configuration for Minecraft $serverVersion")
            }
        }
        
        register<RunServer>("run-$serverVersion") {
            dependsOn(createLog4jConfig)
            minecraftVersion(serverVersion)
            jvmArgs(
                "-DPaper.IgnoreJavaVersion=true", 
                "-Dcom.mojang.eula.agree=true",
                "-Dlog4j.configurationFile=log4j2.xml",
                "-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
            )
            group = "run paper"
            runDirectory.set(file("run-$serverVersion"))
            pluginJars(rootProject.tasks.shadowJar.map { it.archiveFile }.get())
        }
        
        // Create task to check plugin status from logs
        register("checkPluginStatus-$serverVersion") {
            doLast {
                val statusFile = file("run-$serverVersion/logs/plugin-status-$serverVersion.log")
                val exceptionFile = file("run-$serverVersion/logs/exceptions-$serverVersion.log")
                
                if (statusFile.exists()) {
                    println("=== Plugin Status for Minecraft $serverVersion ===")
                    statusFile.readLines().takeLast(10).forEach { println(it) }
                }
                
                if (exceptionFile.exists() && exceptionFile.length() > 0) {
                    println("=== Exceptions found for Minecraft $serverVersion ===")
                    exceptionFile.readLines().takeLast(5).forEach { println(it) }
                } else {
                    println("✅ No exceptions found for Minecraft $serverVersion")
                }
            }
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


paper {
    main = "net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered"
    apiVersion = "1.19"
    authors = listOf("OneLiteFeather", "TheMeinerLP")
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