plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    implementation(platform("com.intellectualsites.bom:bom-1.16.x:1.42"))
    compileOnly("com.plotsquared:PlotSquared-Core")
    compileOnly("com.plotsquared:PlotSquared-Bukkit") { isTransitive = false }
    compileOnly(project(":internal-api"))
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}