# jmxsh Modernization TODO

Improvement opportunities identified by auditing the codebase, build configuration, CI pipeline,
and dependency graph. Items are organized by category. Each item is labeled with a priority:

- 🔴 **Critical** — Bugs, security issues, or blockers
- 🟠 **High** — High-value improvements with clear benefits
- 🟡 **Medium** — Worth doing but lower urgency
- 🟢 **Low** — Polish and nice-to-haves

## Java Language Modernization

- 🟢 **Evaluate `Optional` for nullable return types** — Many methods return null to signal absence; `Optional` would make intent clearer at API boundaries.
- 🟢 **Explore Java 25 features** — Structured concurrency, scoped values, string templates (if stabilized), unnamed patterns, and other features that may simplify existing code.

## Dependency Cleanup

- ✅ ~~**Evaluate migrating JCLI → Picocli**~~ — Evaluation complete. See [`docs/dev/jcli-picocli-evaluation.md`](docs/dev/jcli-picocli-evaluation.md). Recommendation: **migrate**. UX improvements (colored help, better errors, tab completion) outweigh the ~3% jar size increase and one-time migration effort.
- ✅ ~~**Implement JCLI → Picocli migration**~~ — Migration complete. All ~24 command classes, `CommandCenter` argument processing, and tab completion migrated from JCLI/caff to Picocli. See implementation details in the evaluation document.

## Build / POM

- 🟠 **Add `maven-enforcer-plugin` rules** — The plugin is in `pluginManagement` but has no active rules. Add: `requireMavenVersion` (≥ 3.8), `requireJavaVersion` (≥ 25), `banDuplicatePomDependencyVersions`, `dependencyConvergence`.
- 🟡 **Add static analysis plugin** — No static analysis is currently configured. Consider SpotBugs (`spotbugs-maven-plugin`), Checkstyle (`maven-checkstyle-plugin`), or ErrorProne for compile-time bug detection.
- 🟡 **Add `.editorconfig`** — Not present. Ensures consistent indentation, charset, and line endings across IDEs and editors.
- 🟡 **Add `.gitattributes`** — Not present. Normalizes line endings (`* text=auto`, `*.java text eol=lf`, `*.sh text eol=lf`) and marks binary files.

## GitHub Actions / CI

- 🟡 **Enable CodeQL scanning** — No SAST (static application security testing) is configured. Add a CodeQL workflow for Java to catch security issues in PRs.

## Testing

- 🟡 **Add test coverage reporting (JaCoCo)** — No coverage tool is configured. Add `jacoco-maven-plugin` to generate reports and optionally enforce minimum coverage thresholds.
- 🟢 **Address existing TODO in `WatchCommand`** — Line 28: `TODO Consider the use case for CSV file backend generation`. Decide whether to implement or remove the comment.
