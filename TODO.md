# jmxsh Modernization TODO

Improvement opportunities identified by auditing the codebase, build configuration, CI pipeline,
and dependency graph. Items are organized by category. Each item is labeled with a priority:

- 🔴 **Critical** — Bugs, security issues, or blockers
- 🟠 **High** — High-value improvements with clear benefits
- 🟡 **Medium** — Worth doing but lower urgency
- 🟢 **Low** — Polish and nice-to-haves

## Java Language Modernization

- 🟠 **Replace `FileReader`/`FileWriter` with NIO APIs** — These don't specify a charset and are effectively deprecated. Use `Files.newBufferedReader(path, charset)` / `Files.newBufferedWriter(path, charset)`. Affected files: `FileCommandInput.java`, `FileCommandOutput.java`.
- 🟡 **Replace `StringBuffer` with `StringBuilder`** — `StringBuffer` is synchronized and unnecessary here. Affected file: `WatchCommand.java:170`.
- 🟡 **Replace `java.util.Date` with `java.time.Instant`** — The old Date API is discouraged since Java 8. Affected file: `WatchCommand.java:159`.
- 🟡 **Replace `Charset.forName("UTF-8")` with `StandardCharsets.UTF_8`** — Avoids runtime lookup. Affected file: `FileCommandOutputTest.java`.
- 🟡 **Modernize loop patterns** — Convert old-style indexed for-loops and explicit `Iterator` loops to enhanced for or streams where readability improves. Affected files: `RunCommand.java`, `ValueOutputFormat.java`, `AboutCommand.java`.
- 🟡 **Make `Session.output` private** — Currently a `public final` field with an existing TODO comment requesting this change. Add a getter method and update all callers. Affected file: `Session.java:34`.
- 🟡 **Convert try-finally to try-with-resources** — Where the resource implements `AutoCloseable`. Affected file: `SessionImpl.java`.
- 🟢 **Evaluate `Optional` for nullable return types** — Many methods return null to signal absence; `Optional` would make intent clearer at API boundaries.
- 🟢 **Explore Java 25 features** — Structured concurrency, scoped values, string templates (if stabilized), unnamed patterns, and other features that may simplify existing code.

## Dependency Cleanup

- 🟡 **Evaluate migrating JCLI → Picocli** — JCLI (`org.cyclopsgroup:jcli:1.0.2`) is a low-activity library from ~2012. Picocli is actively maintained, has built-in completions, GraalVM support, and better documentation. The coupling is loose (annotations + `ArgumentProcessor`), making migration feasible but non-trivial.

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
