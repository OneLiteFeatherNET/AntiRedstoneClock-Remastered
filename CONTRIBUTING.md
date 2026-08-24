# Contributing

You want to help? Go for it. Look for an open issue, or open one first if you have a new idea —
communication is key, and it saves you from building something we cannot merge.

Ask us anything on [Discord](https://discord.onelitefeather.net).

## Working on the code

- Java 25 is required to build.
- `./gradlew build` compiles, tests and runs the checks.
- `./gradlew run-1.21.11` starts a Paper test server with the plugin installed. There is one
  `run-<version>` task per supported version, and a `run-folia-<version>` task for each of them.
- [docs/development/](docs/development/) covers the dependency injection setup, the testing
  guide and how issues are triaged.

Documentation for users lives in [docs/](docs/index.md) and is published to
<https://onelitefeathernet.github.io/AntiRedstoneClock-Remastered/> from `main`. See
[Writing documentation](docs/development/documentation.md) for how it is organised, how to
preview the site locally, and which pages have to be updated when you change a config key, a
command or a permission.

## Commit messages

Every pull request title and every commit on the branch is checked against
[Conventional Commits](https://www.conventionalcommits.org/) by the `pr-lint` workflow, because
the version number and the changelog are derived from them.

## Releases

We use Semantic Versioning, automated with
[Release Please](https://github.com/googleapis/release-please). Pushing to `main` does not
release anything. Instead, Release Please opens and updates a release pull request that collects
the pending changelog. Merging that pull request is what bumps the version, writes
`CHANGELOG.md`, creates the tag and the GitHub release, and publishes to Hangar and Modrinth.

Which part of the version increases follows from the commit types on the branch:

| Commit | Effect on `MAJOR.MINOR.PATCH` |
|---|---|
| `feat!:` or a `BREAKING CHANGE:` footer | MAJOR — `1.2.3` → `2.0.0` |
| `feat:` | MINOR — `1.2.3` → `1.3.0` |
| `fix:` | PATCH — `1.2.3` → `1.2.4` |
| `docs:`, `style:`, `refactor:`, `test:`, `chore:` | no release |

Dependency updates opened by Renovate go through exactly the same path.

### Example

1. Current version is 1.2.3.
2. `feat: add support for custom clock detection rules` is merged — Release Please opens a
   release pull request for 1.3.0.
3. `fix: prevent false positives in clock detection` is merged — the same pull request is
   updated and still targets 1.3.0.
4. `feat!: redesign configuration format` is merged — the pull request now targets 2.0.0.
5. A maintainer merges it. 2.0.0 is tagged, released and published.

### References

- [Semantic Versioning](https://semver.org/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Release Please](https://github.com/googleapis/release-please)
