import io.papermc.hangarpublishplugin.model.Platforms
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("java")
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.publishdata)
    alias(libs.plugins.paper.run)
    alias(libs.plugins.bukkit.yml)
    alias(libs.plugins.hangar)
    alias(libs.plugins.modrinth)
    id("olf.build-logic")
    `maven-publish`
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

group = "net.onelitefeather"
version = "1.1.1"
val supportedMinecraftVersions = listOf(
    "1.16.5",
    "1.17",
    "1.17.1",
    "1.18",
    "1.18.1",
    "1.18.2",
    "1.19",
    "1.19.1",
    "1.19.2",
    "1.19.3",
    "1.19.4",
    "1.20",
    "1.20.1",
    "1.20.2",
    "1.20.3",
    "1.20.4",
    "1.20.5",
    "1.20.6"
)
allprojects {
    apply {
        plugin("java")
    }
    repositories {
        mavenCentral()
        maven("https://repo.codemc.io/repository/maven-public")
        maven("https://jitpack.io")// Plotsquared V4 Support
        maven("https://maven.enginehub.org/repo/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
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
    implementation(libs.minimessage)
    implementation(libs.cloud.command.paper)
    implementation(libs.cloud.command.extras)
    implementation(libs.cloud.command.annotations)
    implementation("net.kyori:adventure-text-feature-pagination:4.0.0-SNAPSHOT")
    annotationProcessor(libs.cloud.command.annotations)

    implementation(project(":internal-api"))
    implementation(project(":WorldGuardv6Support"))
    implementation(project(":WorldGuardv7Support"))
    implementation(project(":PlotSquaredv4Support"))
    implementation(project(":PlotSquaredv6Support"))
    implementation(project(":PlotSquaredv7Support"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

publishData {
    useEldoNexusRepos(false)
    publishTask("shadowJar")
}

tasks {
    named<Jar>("jar") {
        archiveClassifier.set("unshaded")
    }
    named("build") {
        dependsOn(shadowJar)
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
    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "net.onelitefeather.antiredstoneclockremastered.org.bstats")
    }
}


bukkit {
    main = "net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered"
    apiVersion = "1.16"
    authors = listOf("OneLiteFeather", "TheMeinerLP")
    softDepend = listOf("PlotSquared", "WorldGuard")
    permissions {
        register("antiredstoneclockremastered.notify.admin")
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
val branch = rootProject.branchName()
val baseVersion = publishData.getVersion(false)
val isRelease = !baseVersion.contains('-')
val isMainBranch = branch == "master"
if (!isRelease || isMainBranch) { // Only publish releases from the main branch
    val suffixedVersion =
        if (isRelease) baseVersion else baseVersion + "+" + System.getenv("GITHUB_RUN_NUMBER")
    val changelogContent = if (isRelease) {
        "See [GitHub](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered) for release notes."
    } else {
        val commitHash = rootProject.latestCommitHash()
        "[$commitHash](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/commit/$commitHash) ${rootProject.latestCommitMessage()}"
    }
    hangarPublish {
        publications.register("AntiRedstoneClock-Remastered") {
            version.set(suffixedVersion)
            channel.set(if (isRelease) "Release" else "Snapshot")
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
        versionType.set(if (isRelease) "release" else "beta")
        versionNumber.set(suffixedVersion)
        versionName.set(suffixedVersion)
        changelog.set(changelogContent)
        changelog.set(changelogContent)
        uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
        gameVersions.addAll(supportedMinecraftVersions)
        loaders.add("paper")
        loaders.add("bukkit")
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        // Configure our maven publication
        publishData.configurePublication(this)
    }

    repositories {
        // We add EldoNexus as our repository. The used url is defined by the publish data.
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    // Those credentials need to be set under "Settings -> Secrets -> Actions" in your repository
                    username = System.getenv("ELDO_USERNAME")
                    password = System.getenv("ELDO_PASSWORD")
                }
            }

            name = "EldoNexus"
            setUrl(publishData.getRepository())
        }
    }
}