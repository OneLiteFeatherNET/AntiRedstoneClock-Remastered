dependencies {
    compileOnly(libs.wg6)
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:6.0") {
        exclude("org.bukkit", "bukkit")
    }
}
