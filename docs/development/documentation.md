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

## Building the site

`docs/` is published to GitHub Pages with MkDocs Material by
`.github/workflows/docs.yml`. To see your changes before pushing:

```
python -m venv .venv && . .venv/bin/activate
pip install -r requirements-docs.txt
mkdocs serve
```

The workflow builds with `--strict`, which turns a dead internal link or a page missing from
the `nav` in `mkdocs.yml` into a failed build. A new page therefore has to be added to `nav`,
and it has to be added to `docs/index.md` as well — the index is what readers actually land on.

`docs/development/` is excluded from the published site through `exclude_docs`. It stays
readable on GitHub, which is where the people who need it already are.

## Adding a page

A new page has to be registered in **three** places, and forgetting one of them is the usual
mistake:

1. The file itself, in the directory of its quadrant.
2. `nav:` in `mkdocs.yml` — the MkDocs build runs with `--strict` and fails on a page that is
   missing from it, so this one catches itself.
3. `docs/SUMMARY.md` — the GitBook navigation. **Nothing checks this one.** A page that is not
   listed there is not part of the GitBook space, and a rename that only happens in the file
   system silently drops the page out of the navigation while leaving it reachable by URL.

Also add it to `docs/index.md`, which is what readers actually land on.

## GitBook

`.gitbook.yaml` in the repository root points GitBook Git Sync at `docs/`, with `index.md` as
the landing page and `SUMMARY.md` as the navigation. The four quadrant directories become the
four page groups.

Two things about Git Sync are worth knowing before editing anything in the GitBook editor:

- **It is bidirectional.** Edits made in GitBook are committed back to the branch the space is
  connected to. Those commits do not pass through a pull request, so they are not seen by
  `pr-lint` and not reviewed.
- **It has no link check.** The `--strict` MkDocs build is what catches a dead link between two
  pages, and it only runs on pull requests. A link broken through the GitBook editor is not
  caught by anything until a reader hits it.

While both targets exist, `nav:` in `mkdocs.yml` and `docs/SUMMARY.md` describe the same
structure twice and have to be changed together. That duplication is the reason to settle on
one of the two rather than running both indefinitely.

