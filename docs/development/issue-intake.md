# Issue intake pipeline

How a bug report gets from "someone hit submit" to "a maintainer can act on it",
and what is automated along the way.

## The three problems this solves

1. **Unreadable logs.** The bug template used to collect logs in a plain
   `textarea`. GitHub renders that as Markdown, so timestamps, square brackets
   and stack-trace indentation get mangled and the log becomes unusable. See
   [#267](https://github.com/OneLiteFeatherNET/AntiRedstoneClock-Remastered/issues/267).
2. **Unsupported server software.** Reports from Paper forks, Spigot,
   CraftBukkit and hybrid servers cost triage time for something we do not
   support. See [Scope and non-goals](../explanation/scope-and-non-goals.md).
3. **Issues never reaching the board.** The `projects:` key in an issue form
   only applies when the person opening the issue has write access to the
   project. For outside reporters it is silently ignored, which is why reports
   like #266 and #267 never appeared on the roadmap. Fixed by the project's
   own auto-add automation, which is described under [Board sync](#board-sync).

## What runs, and when

| Stage | Where it lives | Trigger |
|---|---|---|
| Form validation | `.github/ISSUE_TEMPLATE/behavior-bug-or-plugin-incompatibility.yml` | at submit time, in the browser |
| Content checks | `.github/workflows/issue-triage.yml` | `issues: opened, edited, reopened` |
| Board sync | built-in project automations, configured in the project UI | issue created, closed, reopened |

### Form validation

Enforced by GitHub before the issue is created:

- Four required acknowledgement checkboxes (latest version, supported Minecraft
  version, searched for duplicates, pasting text not screenshots).
- A required **Server software** dropdown, so the answer exists even when the
  log is missing.
- `render: text` on every field that receives console output. GitHub wraps
  those in a fenced code block automatically, which is the actual fix for the
  mangled-log problem — the reporter cannot get it wrong any more.
- An optional [mclo.gs](https://mclo.gs) link field for the full log.

### Content checks

`issue-triage.yml` only runs on issues that contain a `### Server Logs`
section, so feature requests are untouched. It never edits or deletes user
content. It:

- detects the server software from the `This server is running X version` line
  the server prints itself. The log outranks the dropdown, so a mis-click does
  not get a Paper user rejected. Detection is deliberately three-way:
  Paper/Folia passes, a name on the known-fork list is closed, and anything
  unrecognised is only flagged for a human — closing on a guess is worse than
  ten seconds of triage;
- verifies the log is in a code block and is actually present;
- compares the reported Minecraft version against `supportedMinecraftVersions`
  in `build.gradle.kts` — the same list that gets published to Hangar and
  Modrinth, so there is one source of truth and no second list to keep in sync;
- compares the reported plugin version against the latest GitHub release;
- writes its findings into **one sticky comment** that is rewritten on every
  edit, rather than piling up new comments.

Outcomes:

| Finding | Label | Closed |
|---|---|---|
| Known Paper fork, Spigot, CraftBukkit, hybrid server | `resolution: invalid` | yes, as *not planned* |
| Server software we do not recognise | `user response` | no |
| Log missing, or not in a code block | `user response` | no |
| Required field empty | `user response` | no |
| Minecraft version not in the supported list | `legacy` + `user response` | no |
| Plugin version older than the latest release | `user response` | no |
| Everything in order | `Game Version: x`, `Plugin Version: x` | no |

Closing only happens for unsupported server software. Everything else stays
open and gets a `user response` label, which is cleared automatically once the
reporter edits the issue into shape.

### Board sync

Board sync deliberately uses **no Actions workflow and no token**. It runs on
the built-in automations of
[the roadmap project](https://github.com/orgs/OneLiteFeatherNET/projects/6),
which GitHub evaluates server-side. Because they belong to the project rather
than to the person opening the issue, they work for outside reporters too -
which is exactly the gap that left #266 and #267 off the board.

A workflow could not do this without a credential anyway: `GITHUB_TOKEN`
cannot write to organisation projects, so any Actions-based approach needs a
PAT or App token.

Four automations are enabled on the project. Together they cover the whole
lifecycle:

| Automation | Filter | Effect |
|---|---|---|
| Auto-add to project | `is:issue is:open` on this repository | puts new issues on the board |
| Item added to project | — | sets Status to New |
| Item closed | — | sets Status to Done |
| Item reopened | — | moves the item back out of Done |

To inspect or change them: project → kebab menu → **Workflows**.

Why the filter is `is:issue is:open` and not just `is:issue`: the auto-add
automation fires on new **or updated** items. With a bare `is:issue`, a
long-closed issue that merely receives a comment would be pulled onto the
board, and "Item added to project" would stamp it Status **New** — a resolved
issue reappearing as fresh work. Restricting to `is:open` avoids that;
reopened issues still come back in through "Item reopened".

Two things to keep in mind:

- **Auto-add is not retroactive.** It only reacts to issues created or updated
  after it was switched on. Issues that were already open at that point were
  added once by hand — at the time of writing that was only #267.
- **The organisation is on the Free plan, which allows exactly one auto-add
  automation per project.** There is no budget for several narrower ones, so
  the single broad filter above has to cover everything. Pro and Team raise
  this to five.

These automations cannot be committed to the repository - the GraphQL API
exposes `deleteProjectV2Workflow` but no create or update counterpart, so the
project UI is the only place to configure them. That also means a reviewer
cannot see them in this repository; the query below prints the live state:

```bash
gh api graphql -f query='
query { organization(login: "OneLiteFeatherNET") { projectV2(number: 6) {
  workflows(first: 30) { nodes { name enabled } } } } }'
```

The `projects:` key is still present in both issue templates. It is harmless
and adds the issue immediately for anyone who has write access to the project,
but it is **not** what makes tracking work - do not remove the auto-add
automation on the assumption that the template covers it.

## Changing the rules

- **Supported Minecraft versions**: edit `supportedMinecraftVersions` in
  `build.gradle.kts`. Triage, Hangar and Modrinth all read that one list.
- **Supported or rejected server software**: the `SUPPORTED` and `FORKS`
  arrays in `issue-triage.yml`.
- **Wording of the automated comment**: the `problems.push(...)` calls in the
  same file.
