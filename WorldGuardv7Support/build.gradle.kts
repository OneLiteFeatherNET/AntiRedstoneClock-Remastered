plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    compileOnly(project(":internal-api"))
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}
