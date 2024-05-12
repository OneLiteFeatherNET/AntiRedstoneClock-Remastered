dependencies {
    implementation(platform(libs.psv6.platform))
    compileOnly(libs.psv6.core)
    compileOnly(libs.psv6.bukkit) { isTransitive = false }
}