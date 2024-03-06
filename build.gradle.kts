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
version = "1.0.1"
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
    "1.20.4"
)

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper)
    implementation(libs.bstats)
    implementation(libs.adventure)
    implementation(libs.cloud.command.paper)
    implementation(libs.cloud.command.extras)
    implementation(libs.cloud.command.annotations)
    annotationProcessor(libs.cloud.command.annotations)

    implementation(project(":internal-api"))
    implementation(project(":WorldGuardv6Support"))
    implementation(project(":WorldGuardv7Support"))
    implementation(project(":PlotSquaredv4Support"))
    implementation(project(":PlotSquaredv6Support"))
    implementation(project(":PlotSquaredv7Support"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishData {
    addMainRepo("")
    addSnapshotRepo("")
    publishTask("shadowJar")
}

tasks {
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

hangarPublish {
    publications.register("AntiRedstoneClock-Remastered") {
        if (publishData.getVersion().contains("SNAPSHOT")) {
            version.set(publishData.getVersion(true))
        } else {
            version.set(publishData.getVersion())
        }
        if (publishData.getVersion().contains("SNAPSHOT")) {
            channel.set("Snapshot")
        } else {
            channel.set("Release")
        }

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
    if (publishData.getVersion().contains("SNAPSHOT")) {
        versionNumber.set(publishData.getVersion(true))
    } else {
        versionNumber.set(publishData.getVersion())
    }
    if (publishData.getVersion().contains("SNAPSHOT")) {
        versionType.set("beta")
    } else {
        versionType.set("release")
    }

    uploadFile.set(tasks.shadowJar as Any)
    gameVersions.addAll(supportedMinecraftVersions)
    loaders.add("paper")
    loaders.add("bukkit")
}