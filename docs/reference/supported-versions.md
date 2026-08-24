# Supported versions

The Minecraft versions the project builds and tests against, and exactly the list published to
Hangar and Modrinth. The authoritative source is `supportedMinecraftVersions` in
`build.gradle.kts` — when a version is added there, it has to be added here too.

- 1.20.6
- 1.21
- 1.21.1
- 1.21.2
- 1.21.3
- 1.21.4
- 1.21.5
- 1.21.6
- 1.21.7
- 1.21.8
- 1.21.9
- 1.21.10
- 1.21.11
- 26.1
- 26.1.1
- 26.1.2
- 26.2

### Declared API version

| | |
|---|---|
| `api-version` | `1.19` |
| Source | `build.gradle.kts`, `paper.apiVersion` |

The plugin loads on servers reporting this API version or newer. Versions outside the
list above are untested and unsupported.

### Server software

| | |
|---|---|
| Paper | supported |
| Folia | supported, `folia-supported: true` |
| Spigot, CraftBukkit, Paper forks, hybrids | not supported |
| Java | 25 or newer |
| Source | `build.gradle.kts`, `paper.foliaSupported` and the Java toolchain |

See [Scope and non-goals](../explanation/scope-and-non-goals.md) for what "not
supported" means in practice.
