dependencies {
    compileOnly(libs.wg6)
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.15") {
        exclude("org.bukkit", "bukkit")
    }
}
