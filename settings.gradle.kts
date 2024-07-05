rootProject.name = "AntiRedstoneClock-Remastered"
includeBuild("build-logic")
include("internal-api")
include("WorldGuardv6Support")
include("WorldGuardv7Support")
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
            version("hangar", "0.1.2")
            version("paper.yml", "0.6.0")
            version("paper.run", "2.3.0")
            version("shadowJar", "8.1.1")

            version("paper", "1.20.6-R0.1-SNAPSHOT")
            version("bstats", "3.0.2")

            version("cloudcommand", "2.0.0-SNAPSHOT")

            version("adventure", "4.17.0")
            version("semver", "0.10.2")

            // WorldGuard
            version("wgv6", "6.2")
            version("wgv7", "7.0.10")
            library("wg6", "com.sk89q.worldguard", "worldguard-legacy").versionRef("wgv6")
            library("wg7", "com.sk89q.worldguard", "worldguard-bukkit").versionRef("wgv7")

            // PlotSquared
            version("psv4", "4.453")
            version("psv6", "1.45")
            version("psv7", "1.45")

            library("psv4.core", "com.github.IntellectualSites.PlotSquared","Core").versionRef("psv4")
            library("psv4.bukkit", "com.github.IntellectualSites.PlotSquared","Bukkit").versionRef("psv4")

            library("psv6.platform", "com.intellectualsites.bom","bom-1.16.x").versionRef("psv6")
            library("psv7.platform", "com.intellectualsites.bom","bom-newest").versionRef("psv7")
            library("psv6.core", "com.plotsquared","PlotSquared-Core").withoutVersion()
            library("psv6.bukkit", "com.plotsquared","PlotSquared-Bukkit").withoutVersion()
            library("psv7.core", "com.intellectualsites.plotsquared","plotsquared-core").withoutVersion()
            library("psv7.bukkit", "com.intellectualsites.plotsquared","plotsquared-bukkit").withoutVersion()

            library("paper", "io.papermc.paper", "paper-api").versionRef("paper")
            library("minimessage", "net.kyori", "adventure-text-minimessage").versionRef("adventure")
            library("bstats", "org.bstats", "bstats-bukkit").versionRef("bstats")

            library("cloud.command.paper", "org.incendo", "cloud-paper").versionRef("cloudcommand")
            library("cloud.command.annotations", "org.incendo", "cloud-annotations").versionRef("cloudcommand")
            library("cloud.command.extras", "org.incendo", "cloud-minecraft-extras").versionRef("cloudcommand")

            library("semver", "com.github.zafarkhaja", "java-semver").versionRef("semver")

            plugin("publishdata","de.chojo.publishdata").versionRef("publishdata")
            plugin("modrinth", "com.modrinth.minotaur").versionRef("modrinth")
            plugin("hangar", "io.papermc.hangar-publish-plugin").versionRef("hangar")
            plugin("paper.yml", "net.minecrell.plugin-yml.paper").versionRef("paper.yml")
            plugin("paper.run", "xyz.jpenilla.run-paper").versionRef("paper.run")
            plugin("shadowJar", "com.github.johnrengelman.shadow").versionRef("shadowJar")
        }
    }
}