rootProject.name = "AntiRedstoneClock-Remastered"
include("internal-api")
include("WorldGuardv6Support")
include("WorldGuardv7Support")
include("PlotSquaredv4Support")
include("PlotSquaredv6Support")
include("PlotSquaredv7Support")

pluginManagement {
    repositories {
        maven("https://eldonexus.de/repository/maven-public/")
        gradlePluginPortal()
    }
}


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("publishdata", "1.4.0")
            version("modrinth", "2.+")
            version("hangar", "0.1.0")
            version("bukkit.yml", "0.6.0")
            version("paper.run", "2.2.0")
            version("shadowJar", "8.1.1")
            version("paper", "1.20.4-R0.1-SNAPSHOT")
            version("bstats", "3.0.2")
            version("cloudcommand", "2.0.0-SNAPSHOT")

            library("paper", "io.papermc.paper", "paper-api").versionRef("paper")
            library("bstats", "org.bstats", "bstats-bukkit").versionRef("bstats")
            library("cloud.command.paper", "org.incendo", "cloud-paper").versionRef("cloudcommand")
            library("cloud.command.annotations", "org.incendo", "cloud-annotations").versionRef("cloudcommand")
            library("cloud.command.extras", "org.incendo", "cloud-minecraft-extras").versionRef("cloudcommand")

            plugin("publishdata","de.chojo.publishdata").versionRef("publishdata")
            plugin("modrinth", "com.modrinth.minotaur").versionRef("modrinth")
            plugin("hangar", "io.papermc.hangar-publish-plugin").versionRef("hangar")
            plugin("bukkit.yml", "net.minecrell.plugin-yml.bukkit").versionRef("bukkit.yml")
            plugin("paper.run", "xyz.jpenilla.run-paper").versionRef("paper.run")
            plugin("shadowJar", "com.github.johnrengelman.shadow").versionRef("shadowJar")
        }
    }
}