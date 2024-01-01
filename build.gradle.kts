import io.papermc.hangarpublishplugin.model.Platforms
import org.ajoberstar.grgit.Grgit
import xyz.jpenilla.runpaper.task.RunServer
import java.util.*

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.2.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("io.papermc.hangar-publish-plugin") version "0.1.1"
    id("com.modrinth.minotaur") version "2.+"
    id("org.jetbrains.changelog") version "2.2.0"
    id("org.ajoberstar.grgit") version "5.2.1"
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
var baseVersion by extra("1.0.0")
var extension by extra("")
var snapshot by extra("-SNAPSHOT")
ext {
    val git: Grgit = Grgit.open {
        dir = File("$rootDir/.git")
    }
    val revision = git.head().abbreviatedId
    extension = "%s+%s".format(Locale.ROOT, snapshot, revision)
}

version = "%s%s".format(Locale.ROOT, baseVersion, extension)

val minecraftVersion = "1.20.1"
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
)

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation(project(":internal-api"))
    implementation(project(":WorldGuardv6Support"))
    implementation(project(":WorldGuardv7Support"))
    implementation(project(":PlotSquaredv4Support"))
    implementation(project(":PlotSquaredv6Support"))
    implementation(project(":PlotSquaredv7Support"))
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
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
    }
}

changelog {
    version.set(baseVersion)
    path.set("${project.projectDir}/CHANGELOG.md")
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
}

hangarPublish {
    publications.register("AntiRedstoneClock-Remastered") {
        version.set(project.version.toString())
        channel.set(System.getenv("HANGAR_CHANNEL"))
        changelog.set(
                project.changelog.renderItem(
                        project.changelog.getOrNull(baseVersion) ?: project.changelog.getUnreleased()
                )
        )
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
    projectId.set("UWh9tyEa") //TODO: Change
    versionNumber.set(version.toString())
    versionType.set(System.getenv("MODRINTH_CHANNEL"))
    uploadFile.set(tasks.shadowJar as Any)
    gameVersions.addAll(supportedMinecraftVersions)
    loaders.add("paper")
    loaders.add("bukkit")
    changelog.set(
            project.changelog.renderItem(
                    project.changelog.getOrNull(baseVersion) ?: project.changelog.getUnreleased()
            )
    )
}