// Conventional Commits enforcement (commits + PR title).
// The types listed here must stay in sync with .github/workflows/pr-lint.yml
// and with the changelog-sections in release-please-config.json.
// https://www.conventionalcommits.org
export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    // Renovate and release commits carry long URLs / tables in the body.
    'body-max-line-length': [0, 'always'],
    'footer-max-line-length': [0, 'always'],
    'header-max-length': [2, 'always', 100],
    'type-enum': [
      2,
      'always',
      ['build', 'chore', 'ci', 'docs', 'feat', 'fix', 'perf', 'refactor', 'revert', 'style', 'test'],
    ],
  },
};
