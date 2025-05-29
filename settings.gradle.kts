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
            version("hangar", "0.1.3")
            version("paper.yml", "0.6.0")
            version("paper.run", "2.3.1")
            version("shadowJar", "9.0.0-beta15")

            version("paper", "1.20.6-R0.1-SNAPSHOT")
            version("bstats", "3.1.0")

            version("cloudcommand", "2.0.0")
            version("cloudcommandPaper", "2.0.0-SNAPSHOT")
            version("cloudcommandAnnotations", "2.0.0")
            version("cloudcommandExtras", "2.0.0-SNAPSHOT")

            version("adventure", "4.17.0")
            version("adventure-text-feature-pagination", "4.0.0-SNAPSHOT")
            version("semver", "0.10.2")

            // WorldGuard
            version("wgv6", "6.2")
            version("wgv7", "7.0.13")
            library("wg6", "com.sk89q.worldguard", "worldguard-legacy").versionRef("wgv6")
            library("wg7", "com.sk89q.worldguard", "worldguard-bukkit").versionRef("wgv7")

            // PlotSquared
            version("psv4", "4.453")
            version("psv6", "1.52")
            version("psv7", "1.52")

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
            library("adventure.text.feature.pagination", "net.kyori", "adventure-text-feature-pagination").versionRef("adventure-text-feature-pagination")
            library("bstats", "org.bstats", "bstats-bukkit").versionRef("bstats")

            library("cloud.command.paper", "org.incendo", "cloud-paper").versionRef("cloudcommandPaper")
            library("cloud.command.annotations", "org.incendo", "cloud-annotations").versionRef("cloudcommandAnnotations")
            library("cloud.command.extras", "org.incendo", "cloud-minecraft-extras").versionRef("cloudcommandExtras")

            library("semver", "com.github.zafarkhaja", "java-semver").versionRef("semver")

            plugin("publishdata","de.chojo.publishdata").versionRef("publishdata")
            plugin("modrinth", "com.modrinth.minotaur").versionRef("modrinth")
            plugin("hangar", "io.papermc.hangar-publish-plugin").versionRef("hangar")
            plugin("paper.yml", "net.minecrell.plugin-yml.paper").versionRef("paper.yml")
            plugin("paper.run", "xyz.jpenilla.run-paper").versionRef("paper.run")
            plugin("shadowJar", "com.gradleup.shadow").versionRef("shadowJar")
        }
    }
}