import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import xyz.jpenilla.runpaper.task.RunServer
import xyz.jpenilla.runtask.pluginsapi.PluginDownloadService
import xyz.jpenilla.runtask.service.DownloadsAPIService

plugins {
    id("java")
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.paper.run)
    alias(libs.plugins.paper.yml)
    alias(libs.plugins.hangar)
    alias(libs.plugins.modrinth)
    alias(libs.plugins.cyclonedx)
    jacoco
}

version = "2.10.0" // x-release-please-version

if (!File("$rootDir/.git").exists()) {
    logger.lifecycle(
        """
    **************************************************************************************
    You need to fork and clone this repository! Don't download a .zip file.
    If you need assistance, consult the GitHub docs: https://docs.github.com/get-started/quickstart/fork-a-repo
    **************************************************************************************
    """.trimIndent()
    ).also { System.exit(1) }
}
val supportedMinecraftVersions = listOf(
    "1.20.6",
    "1.21",
    "1.21.1",
    "1.21.2",
    "1.21.3",
    "1.21.4",
    "1.21.5",
    "1.21.6",
    "1.21.7",
    "1.21.8",
    "1.21.9",
    "1.21.10",
    "1.21.11",
    "26.1",
    "26.1.1",
    "26.1.2",
    "26.2"
)
allprojects {
    apply {
        plugin("java")
    }

    version = rootProject.version

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        if (name != "internal-api") {
            compileOnly(project(":internal-api"))
        }
        compileOnly(rootProject.libs.paper)
    }
}

dependencies {
    constraints {
        // CVE-2023-5072 (HIGH): org.json below 20231013 can be driven into a
        // parser denial of service. It reaches the shaded jar transitively via
        // club.minnced:discord-webhooks, which still resolves 20230618.
        // Found by the Trivy gate in .github/workflows/release-please.yml.
        implementation("org.json:json:20231013") {
            because("CVE-2023-5072 in the version discord-webhooks pulls in")
        }
    }

    implementation(libs.bstats)
    implementation(libs.cloud.command.paper)
    implementation(libs.cloud.command.extras)
    implementation(libs.cloud.command.annotations)
    implementation(libs.semver)
    implementation(libs.adventure.text.discord)
    implementation(libs.jda.webhook)
    implementation(libs.customblockdata)
    implementation(libs.guice)
    implementation(libs.jakarta.inject)
    annotationProcessor(libs.cloud.command.annotations)

    implementation(project(":internal-api"))
    implementation(project(":WorldGuardv6Support"))
    implementation(project(":WorldGuardv7Support"))
    implementation(project(":PlotSquaredv6Support"))
    implementation(project(":PlotSquaredv7Support"))

    compileOnly(libs.log4j.core)

    // Testing dependencies
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.log4j.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.mockbukkit)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks {
    named("build") {
        dependsOn(shadowJar)
    }
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = false
        }
        maxParallelForks = 1
        
        // Generate test reports
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
        
        // Test result publication
        finalizedBy(jacocoTestReport)
    }
    supportedMinecraftVersions.forEach { serverVersion ->
        register<RunServer>("run-$serverVersion") {
            minecraftVersion(serverVersion)
            jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
            group = "run paper"
            runDirectory.set(file("run-$serverVersion"))
            pluginJars(rootProject.tasks.shadowJar.map { it.archiveFile }.get())
        }
    }
    supportedMinecraftVersions.forEach { serverVersion ->
        register<RunServer>("run-folia-$serverVersion") {
            minecraftVersion(serverVersion)
            jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
            group = "run folia"
            runDirectory.set(file("run-folia-$serverVersion"))
            pluginJars(rootProject.tasks.shadowJar.map { it.archiveFile }.get())
            downloadsApiService.convention(DownloadsAPIService.folia(project))
            pluginDownloadService.convention(PluginDownloadService.paper(project))
        }
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "net.onelitefeather.antiredstoneclockremastered.org.bstats")
        relocate("com.jeff_media.customblockdata", "net.onelitefeather.antiredstoneclockremastered.com.jeff_media.customblockdata")
        // Pulled in transitively by club.minnced:discord-webhooks -> okhttp -> kotlin-stdlib.
        // An unrelocated kotlin/ package makes eco flag this plugin as conflicting (#216).
        relocate("kotlin", "net.onelitefeather.antiredstoneclockremastered.kotlin")
        relocate("kotlinx", "net.onelitefeather.antiredstoneclockremastered.kotlinx")
        relocate("okhttp3", "net.onelitefeather.antiredstoneclockremastered.okhttp3")
        relocate("okio", "net.onelitefeather.antiredstoneclockremastered.okio")
        dependsOn(jar)
    }
    this.modrinth {
        dependsOn(shadowJar)
    }

    this.publishAllPublicationsToHangar {
        dependsOn(shadowJar)
    }
    
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

runPaper.folia {
    registerTask()
}


paper {
    main = "net.onelitefeather.antiredstoneclockremastered.AntiRedstoneClockRemastered"
    apiVersion = "1.19"
    authors = listOf("OneLiteFeather", "TheMeinerLP")
    foliaSupported = true
    serverDependencies {
        register("PlotSquared") {
            required = false
        }
        register("WorldGuard") {
            required = false
        }
    }
    permissions {
        register("antiredstoneclockremastered.notify.admin") {
            description = "Receive the in-game message when a redstone clock is detected. The holder must also be a server operator."
        }
        register("antiredstoneclockremastered.notify.disable.donation") {
            description = "Suppress the donation notice that is shown on join."
        }
        register("antiredstoneclockremastered.notify.admin.update") {
            description = "Receive a notice on join when a newer plugin version is available on Hangar."
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("antiredstoneclockremastered.command.reload") {
            description = "Use /arcm reload to re-read config.yml."
        }
        register("antiredstoneclockremastered.command.help") {
            description = "Use /arcm help to list the commands and their descriptions."
        }
        register("antiredstoneclockremastered.command.display") {
            description = "Use /arcm display to page through the redstone clocks currently under observation."
        }
        register("antiredstoneclockremastered.command.feature.check.observer") {
            description = "Toggle detection of observers with /arcm feature check observer."
        }
        register("antiredstoneclockremastered.command.feature.check.piston") {
            description = "Toggle detection of pistons with /arcm feature check piston."
        }
        register("antiredstoneclockremastered.command.feature.check.sculk") {
            description = "Toggle detection of sculk sensors with /arcm feature check sculk."
        }
        register("antiredstoneclockremastered.command.feature.check.redstone_and_repeater") {
            description = "Toggle detection of redstone dust and repeaters with /arcm feature check redstone_and_repeater."
        }
        register("antiredstoneclockremastered.command.feature.check.hopper") {
            description = "Toggle detection of hopper clocks with /arcm feature check hopper."
        }
        register("antiredstoneclockremastered.command.feature.check.world.add") {
            description = "Put a world on the ignore list with /arcm feature check ignored_worlds add <world>."
        }
        register("antiredstoneclockremastered.command.feature.check.world.remove") {
            description = "Take a world off the ignore list with /arcm feature check ignored_worlds remove <world>."
        }
        register("antiredstoneclockremastered.command.feature.check.region.remove") {
            description = "Take a WorldGuard region off the ignore list with /arcm feature check ignored_regions remove <region>."
        }
        register("antiredstoneclockremastered.command.feature.check.region.add") {
            description = "Put a WorldGuard region on the ignore list with /arcm feature check ignored_regions add <region>."
        }
        register("antiredstoneclockremastered.command.feature.clock.notifyAdmins") {
            description = "Toggle the in-game admin notification with /arcm feature clock notify_admins."
        }
        register("antiredstoneclockremastered.command.feature.clock.notifyConsole") {
            description = "Toggle the console notification with /arcm feature clock notify_console."
        }
        register("antiredstoneclockremastered.command.feature.clock.drop") {
            description = "Toggle whether a removed clock drops its items with /arcm feature clock drop."
        }
        register("antiredstoneclockremastered.command.feature.clock.enddelay") {
            description = "Set how long a block stays under observation with /arcm feature clock endDelay <delay>."
        }
        register("antiredstoneclockremastered.command.feature.clock.maxCount") {
            description = "Set the trigger limit with /arcm feature clock maxCount <count>."
        }
        register("antiredstoneclockremastered.command.feature.debug") {
            description = "Toggle debug logging with /arcm feature debug."
        }
        register("antiredstoneclockremastered.bundle.admin") {
            children = listOf(
                "antiredstoneclockremastered.notify.admin",
                "antiredstoneclockremastered.notify.disable.donation",
                "antiredstoneclockremastered.notify.admin.update",
                "antiredstoneclockremastered.command.reload",
                "antiredstoneclockremastered.command.help",
                "antiredstoneclockremastered.command.feature.check.observer",
                "antiredstoneclockremastered.command.feature.check.piston",
                "antiredstoneclockremastered.command.feature.check.sculk",
                "antiredstoneclockremastered.command.feature.check.redstone_and_repeater",
                "antiredstoneclockremastered.command.feature.check.hopper",
                "antiredstoneclockremastered.command.feature.check.world.add",
                "antiredstoneclockremastered.command.feature.check.world.remove",
                "antiredstoneclockremastered.command.feature.check.region.remove",
                "antiredstoneclockremastered.command.feature.check.region.add",
                "antiredstoneclockremastered.command.feature.clock.notifyAdmins",
                "antiredstoneclockremastered.command.feature.clock.notifyConsole",
                "antiredstoneclockremastered.command.feature.clock.drop",
                "antiredstoneclockremastered.command.feature.clock.enddelay",
                "antiredstoneclockremastered.command.feature.clock.maxCount",
                "antiredstoneclockremastered.command.feature.debug",
                "antiredstoneclockremastered.command.display"
            )
            default = BukkitPluginDescription.Permission.Default.OP
            description = "All permissions for AntiRedstoneClock-Remastered"
        }

        register("antiredstoneclockremastered.bundle.developers") {
            children = listOf(
                "antiredstoneclockremastered.command.reload",
                "antiredstoneclockremastered.command.help",
                "antiredstoneclockremastered.command.display"
            )
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Permissions for developers of AntiRedstoneClock-Remastered"
        }
    }
}
val baseVersion = version as String
val baseChannel = with(baseVersion) {
    when {
        contains("SNAPSHOT", true) -> "Snapshot"
        contains("ALPHA", true) -> "Alpha"
        contains("BETA", true) -> "Beta"
        else -> "Release"
    }
}
val changelogContent = "See [GitHub](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/releases/tag/$baseVersion) for release notes."
hangarPublish {
    publications.register("AntiRedstoneClock-Remastered") {
        version.set(baseVersion)
        channel.set(baseChannel)
        changelog.set(changelogContent)
        apiKey.set(System.getenv("HANGAR_SECRET"))
        id.set("AntiRedstoneClock-Remastered")

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set(supportedMinecraftVersions)
            }
        }
    }
}
modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("UWh9tyEa")
    versionType.set(baseChannel.lowercase())
    versionNumber.set(baseVersion)
    versionName.set(baseVersion)
    changelog.set(changelogContent)
    changelog.set(changelogContent)
    uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    gameVersions.addAll(supportedMinecraftVersions)
    loaders.add("paper")
    loaders.add("bukkit")
    loaders.add("folia")
}
// ---------------------------------------------------------------------------------------------
// Reference documentation generation
//
// The reference quadrant of docs/ is generated from the same sources the software itself reads,
// so the two cannot disagree. See docs/reference/ for the generated pages and
// docs/development/documentation.md for the workflow.
// ---------------------------------------------------------------------------------------------

/** A permission as declared in the `paper { permissions { ... } }` block. */
data class DeclaredPermission(
    val name: String,
    val description: String?,
    val default: String?,
    val children: List<String>
)

/**
 * Reads the permissions out of the generated paper-plugin.yml.
 *
 * The file is flat and machine generated, so it is parsed line by line rather than pulling a
 * YAML library into the build classpath.
 */
fun parseDeclaredPermissions(pluginYml: File): List<DeclaredPermission> {
    val lines = pluginYml.readLines().map { it.removeSuffix("\r") }
    val start = lines.indexOfFirst { it == "permissions:" }
    if (start < 0) return emptyList()

    val permissions = mutableListOf<DeclaredPermission>()
    var name: String? = null
    var description: String? = null
    var default: String? = null
    var children = mutableListOf<String>()
    var inChildren = false

    fun flush() {
        name?.let { permissions += DeclaredPermission(it, description, default, children.toList()) }
        name = null; description = null; default = null; children = mutableListOf(); inChildren = false
    }

    for (line in lines.drop(start + 1)) {
        if (line.isBlank()) continue
        val indent = line.takeWhile { it == ' ' }.length
        if (indent == 0) break // next top level key ends the permissions block
        val content = line.trim()
        when (indent) {
            2 -> {
                flush()
                name = content.removeSuffix(":").removeSuffix(" {}").trim().removeSuffix(":")
            }
            4 -> {
                inChildren = false
                when {
                    content == "children:" -> inChildren = true
                    content.startsWith("description:") ->
                        description = content.removePrefix("description:").trim().trim('"')
                    content.startsWith("default:") ->
                        default = content.removePrefix("default:").trim().trim('"')
                }
            }
            6 -> if (inChildren) children += content.substringBefore(":").trim()
        }
    }
    flush()
    return permissions.sortedBy { it.name }
}

fun renderPermissionsPage(permissions: List<DeclaredPermission>): String = buildString {
    appendLine("# Permissions")
    appendLine()
    appendLine("Generated from the `paper { permissions { ... } }` block in `build.gradle.kts` by")
    appendLine("`./gradlew generateReferenceDocs`. Do not edit by hand.")
    appendLine()
    appendLine("These are the permissions the plugin declares. LuckPerms and comparable plugins read")
    appendLine("them from the JAR and suggest them automatically. A permission the plugin checks but")
    appendLine("does not declare is not listed here; those are named per command in")
    appendLine("[Commands](commands.md).")
    appendLine()
    appendLine("A permission without a stated default is held by server operators only.")
    appendLine()
    for (permission in permissions) {
        appendLine("### `${permission.name}`")
        appendLine()
        appendLine("| | |")
        appendLine("|---|---|")
        appendLine("| Default | ${permission.default ?: "not declared, operators only"} |")
        if (permission.children.isNotEmpty()) {
            appendLine("| Grants | ${permission.children.size} child permissions |")
        }
        appendLine("| Source | `build.gradle.kts`, `paper.permissions` |")
        appendLine()
        permission.description?.let { appendLine(it); appendLine() }
        if (permission.children.isNotEmpty()) {
            for (child in permission.children.sorted()) appendLine("- `$child`")
            appendLine()
        }
    }
}

fun renderSupportedVersionsPage(versions: List<String>, apiVersion: String): String = buildString {
    appendLine("# Supported versions")
    appendLine()
    appendLine("Generated from `supportedMinecraftVersions` and `paper.apiVersion` in `build.gradle.kts`")
    appendLine("by `./gradlew generateReferenceDocs`. Do not edit by hand.")
    appendLine()
    appendLine("These are the Minecraft versions the project builds and tests against, and exactly the")
    appendLine("list published to Hangar and Modrinth.")
    appendLine()
    for (v in versions) appendLine("- $v")
    appendLine()
    appendLine("### Declared API version")
    appendLine()
    appendLine("| | |")
    appendLine("|---|---|")
    appendLine("| `api-version` | `$apiVersion` |")
    appendLine("| Source | `build.gradle.kts`, `paper.apiVersion` |")
    appendLine()
    appendLine("The plugin loads on servers reporting this API version or newer. Versions outside the")
    appendLine("list above are untested and unsupported.")
    appendLine()
    appendLine("### Server software")
    appendLine()
    appendLine("| | |")
    appendLine("|---|---|")
    appendLine("| Paper | supported |")
    appendLine("| Folia | supported, `folia-supported: true` |")
    appendLine("| Spigot, CraftBukkit, Paper forks, hybrids | not supported |")
    appendLine("| Java | 25 or newer |")
    appendLine("| Source | `build.gradle.kts`, `paper.foliaSupported` and the Java toolchain |")
    appendLine()
    appendLine("See [Scope and non-goals](../explanation/scope-and-non-goals.md) for what \"not")
    appendLine("supported\" means in practice.")
}

/** Every fully qualified key in a config.yml, mapped to its literal value where it has a scalar one. */
fun parseConfigKeys(configYml: File): Map<String, String?> {
    val keys = linkedMapOf<String, String?>()
    val path = mutableListOf<String>()
    var blockScalarIndent: Int? = null
    var listIndent: Int? = null

    for (line0 in configYml.readLines()) {
        val raw = line0.removeSuffix("\r")
        val indent = raw.takeWhile { it == ' ' }.length
        if (blockScalarIndent != null) {
            if (raw.isBlank() || indent > blockScalarIndent!!) continue
            blockScalarIndent = null
        }
        // A list item and everything nested under it belongs to the key above, including the
        // keys of a list of maps such as notification.discord.fields.
        if (listIndent != null) {
            if (raw.isBlank() || indent >= listIndent!!) continue
            listIndent = null
        }
        val line = raw.trim()
        if (line.isEmpty() || line.startsWith("#")) continue
        if (line.startsWith("- ") || line == "-") { listIndent = indent; continue }

        val depth = indent / 2
        while (path.size > depth) path.removeAt(path.size - 1)

        val key = line.substringBefore(":").trim()
        val value = line.substringAfter(":", "").substringBefore(" #").trim()
        path += key
        val qualified = path.joinToString(".")
        when {
            value == "|" || value == ">" -> { keys[qualified] = null; blockScalarIndent = indent }
            value.isEmpty() -> keys[qualified] = null // section, or a key whose value is a list
            else -> keys[qualified] = value
        }
    }
    return keys
}

val referenceDocsDir = layout.projectDirectory.dir("docs/reference")

val generateReferenceDocs = tasks.register("generateReferenceDocs") {
    group = "documentation"
    description = "Writes the generated pages of docs/reference/ from build.gradle.kts."
    dependsOn("generatePaperPluginDescription")

    val pluginYml = layout.buildDirectory.file("generated/plugin-yml/Paper/paper-plugin.yml")
    val versions = supportedMinecraftVersions
    val apiVersion = "1.19"
    val outputDir = referenceDocsDir

    inputs.file(pluginYml)
    inputs.property("versions", versions)
    inputs.property("apiVersion", apiVersion)
    outputs.file(outputDir.file("permissions.md"))
    outputs.file(outputDir.file("supported-versions.md"))

    doLast {
        val permissions = parseDeclaredPermissions(pluginYml.get().asFile)
        outputDir.file("permissions.md").asFile.writeText(renderPermissionsPage(permissions))
        outputDir.file("supported-versions.md").asFile.writeText(renderSupportedVersionsPage(versions, apiVersion))
        logger.lifecycle("Wrote ${permissions.size} permissions and ${versions.size} versions to docs/reference/")
    }
}

val checkReferenceDocs = tasks.register("checkReferenceDocs") {
    group = "verification"
    description = "Fails when docs/reference/ has drifted away from the sources it is generated from."
    dependsOn("generatePaperPluginDescription")

    val pluginYml = layout.buildDirectory.file("generated/plugin-yml/Paper/paper-plugin.yml")
    val configYml = layout.projectDirectory.file("src/main/resources/config.yml")
    val versions = supportedMinecraftVersions
    val apiVersion = "1.19"
    val outputDir = referenceDocsDir

    doLast {
        val problems = mutableListOf<String>()

        // Checkouts on Windows turn the committed LF into CRLF, so both sides of every
        // comparison are normalised before they are compared.
        fun File.readTextLf() = readText().replace("\r\n", "\n")

        fun compare(fileName: String, expected: String) {
            val actual = outputDir.file(fileName).asFile
            if (!actual.exists()) problems += "docs/reference/$fileName is missing"
            else if (actual.readTextLf() != expected) problems += "docs/reference/$fileName is out of date"
        }

        compare("permissions.md", renderPermissionsPage(parseDeclaredPermissions(pluginYml.get().asFile)))
        compare("supported-versions.md", renderSupportedVersionsPage(versions, apiVersion))

        // The configuration reference carries hand written effect sentences and is therefore not
        // generated, but every key and every default in it is checked against the shipped config.yml.
        val configurationPage = outputDir.file("configuration.md").asFile
        if (!configurationPage.exists()) {
            problems += "docs/reference/configuration.md is missing"
        } else {
            val page = configurationPage.readTextLf()
            val documented = Regex("""^### `([^`]+)`""", RegexOption.MULTILINE)
                .findAll(page).map { it.groupValues[1] }.toList()
            val shipped = parseConfigKeys(configYml.asFile)
            // Sections exist only to nest keys and are not documented in their own right.
            val leaves = shipped.keys.filter { key -> shipped.keys.none { it.startsWith("$key.") } }

            (leaves - documented.toSet()).forEach {
                problems += "config.yml key `$it` is not documented in docs/reference/configuration.md"
            }
            (documented - shipped.keys).forEach {
                problems += "docs/reference/configuration.md documents `$it`, which config.yml does not contain"
            }
            for (key in documented.intersect(leaves.toSet())) {
                val shippedDefault = shipped[key] ?: continue // lists and block scalars carry no inline literal
                val section = page.substringAfter("### `$key`").substringBefore("\n### ")
                val documentedDefault = Regex("""\|\s*Default\s*\|\s*`?([^`|]*)`?\s*\|""")
                    .find(section)?.groupValues?.get(1)?.trim()
                if (documentedDefault != null && documentedDefault != shippedDefault) {
                    problems += "`$key`: config.yml ships `$shippedDefault`, the reference says `$documentedDefault`"
                }
            }
        }

        if (problems.isNotEmpty()) {
            throw GradleException(
                problems.joinToString(
                    separator = "\n  - ",
                    prefix = "The reference documentation has drifted:\n  - ",
                    postfix = "\n\nRun ./gradlew generateReferenceDocs and commit the result, " +
                        "or update docs/reference/configuration.md to match src/main/resources/config.yml."
                )
            )
        }
    }
}

tasks.named("check") {
    dependsOn(checkReferenceDocs)
}
