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

## Generated pages

Two reference pages are generated and must not be edited by hand:

| Page | Generated from |
|---|---|
| `reference/permissions.md` | the `paper { permissions { ... } }` block, via the generated `paper-plugin.yml` |
| `reference/supported-versions.md` | `supportedMinecraftVersions` and `paper.apiVersion` |

Regenerate and commit the result with:

```
./gradlew generateReferenceDocs
```

The one sentence a generator cannot produce is what a permission actually does. That sentence is
the `description` next to the permission in `build.gradle.kts`, so the generator carries it
along — and permission plugins show it to server admins in game as a bonus.

`reference/configuration.md` is hand-written, because each key needs an effect sentence that
exists nowhere in the source. It is still guarded: every key and every default in it is checked
against `src/main/resources/config.yml`.

## The drift gate

```
./gradlew checkReferenceDocs
```

runs as part of `check`, so `./gradlew build` and the `build-pr` workflow fail when:

- a generated page differs from what the generator would write now,
- `config.yml` gained a key that `reference/configuration.md` does not document,
- `reference/configuration.md` documents a key `config.yml` does not have,
- a default in the reference disagrees with the one shipped in `config.yml`.

Adding a config key therefore fails the build until the reference is updated. That is the point:
it turns "the docs are out of date" from a maintenance task into a build error.
