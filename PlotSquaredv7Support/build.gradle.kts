dependencies {
    implementation(platform(libs.psv7.platform))
    compileOnly(libs.psv7.core)
    compileOnly(libs.psv7.bukkit) { isTransitive = false }
}