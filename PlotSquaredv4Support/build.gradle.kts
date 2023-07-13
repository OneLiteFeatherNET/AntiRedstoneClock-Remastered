plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public")
    maven("https://jitpack.io")// Plotsquared V4 Support
    maven("https://maven.enginehub.org/repo/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("com.github.IntellectualSites.PlotSquared:Core:4.453")
    compileOnly("com.github.IntellectualSites.PlotSquared:Bukkit:4.453")
    compileOnly(project(":internal-api"))
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}