# Writing documentation

User-facing documentation lives in `docs/` and is organised by what the reader is doing, not by
feature. The four directories are not interchangeable:

| Directory | For a reader who | Rule of thumb |
|---|---|---|
| `tutorial/` | is learning by doing | exactly one page, exactly one path, no alternatives |
| `how-to/` | has a goal | title starts with a verb, one goal per page |
| `reference/` | is looking something up | facts only, no advice, same fields in the same order |
| `explanation/` | wants to understand | prose, trade-offs, scope boundaries, no numbered steps |

`docs/development/` is not a fifth quadrant. It holds the operational record for people working
on the plugin — architecture notes, testing, issue triage — and is linked from
`CONTRIBUTING.md`, not from the user documentation index.

Two rules keep the set from rotting:

- **A value appears in exactly one place.** A how-to names a config key and links to the
  reference; it never restates the default. Restated values are the main source of drift.
- **Recommendations never go in `reference/`.** In a reference table a recommendation is read as
  a fact, and unlike a fact it goes stale silently. It belongs in `explanation/`.

## Keeping the reference honest

Every page under `docs/reference/` is written by hand, so nothing enforces that it still matches
the code. These four changes are the ones that silently invalidate it — update the page in the
same commit:

| When you change | Update |
|---|---|
| `paper { permissions { ... } }` in `build.gradle.kts` | `reference/permissions.md` |
| `supportedMinecraftVersions` or `paper.apiVersion` | `reference/supported-versions.md` |
| a key or default in `src/main/resources/config.yml` | `reference/configuration.md` |
| a command, its arguments or its `@Permission` | `reference/commands.md` |

Two habits make that cheap:

- **Give every declared permission a `description` in `build.gradle.kts`.** It is the one
  sentence saying what the permission actually does, it lives next to the declaration where it
  cannot be missed, and it ends up in the `plugin.yml` inside the jar — so permission plugins
  show it to server admins as well.
- **Never restate a default outside `reference/configuration.md`.** A how-to names the key and
  links to the reference. A value that exists in two places is a contradiction waiting to happen,
  and it is the reason the old README and the code disagreed on several points.
