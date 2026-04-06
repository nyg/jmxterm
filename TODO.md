# JMXTerm Modernization TODO

Improvement opportunities identified by auditing the codebase, build configuration, CI pipeline,
and dependency graph. Items are organized by category. Each item is labeled with a priority:

- 🔴 **Critical** — Bugs, security issues, or blockers
- 🟠 **High** — High-value improvements with clear benefits
- 🟡 **Medium** — Worth doing but lower urgency
- 🟢 **Low** — Polish and nice-to-haves

---

## Java Language Modernization

Target: **Java 25** (upcoming LTS, September 2025).

- 🟠 **Upgrade compiler target from 17 to 25** — Update `<release>17</release>` in pom.xml. Enables records, sealed classes, pattern matching, virtual threads, and other post-17 features across the codebase.
- 🟠 **Replace `FileReader`/`FileWriter` with NIO APIs** — These don't specify a charset and are effectively deprecated. Use `Files.newBufferedReader(path, charset)` / `Files.newBufferedWriter(path, charset)`. Affected files: `FileCommandInput.java`, `FileCommandOutput.java`.
- 🟡 **Replace `StringBuffer` with `StringBuilder`** — `StringBuffer` is synchronized and unnecessary here. Affected file: `WatchCommand.java:170`.
- 🟡 **Replace `java.util.Date` with `java.time.Instant`** — The old Date API is discouraged since Java 8. Affected file: `WatchCommand.java:159`.
- 🟡 **Replace `Charset.forName("UTF-8")` with `StandardCharsets.UTF_8`** — Avoids runtime lookup. Affected file: `FileCommandOutputTest.java`.
- 🟡 **Modernize loop patterns** — Convert old-style indexed for-loops and explicit `Iterator` loops to enhanced for or streams where readability improves. Affected files: `RunCommand.java`, `ValueOutputFormat.java`, `AboutCommand.java`.
- 🟡 **Make `Session.output` private** — Currently a `public final` field with an existing TODO comment requesting this change. Add a getter method and update all callers. Affected file: `Session.java:34`.
- 🟡 **Convert try-finally to try-with-resources** — Where the resource implements `AutoCloseable`. Affected file: `SessionImpl.java`.
- 🟢 **Evaluate `Optional` for nullable return types** — Many methods return null to signal absence; `Optional` would make intent clearer at API boundaries.
- 🟢 **Explore Java 25 features** — Structured concurrency, scoped values, string templates (if stabilized), unnamed patterns, and other features that may simplify existing code.

---

## Dependency Cleanup

Several dependencies are unused or barely used and can be replaced with JDK standard library equivalents.

- 🔴 **Remove Guava** — Declared in pom.xml (`com.google.guava:guava:33.5.0-jre`) but has **zero imports** anywhere in the codebase. Pure dead weight (~3 MB in uber JAR).
- 🟠 **Remove `commons-collections4`** — Only used for `ListOrderedMap` in 2 files (`GetCommand.java`, `ValueOutputFormatTest.java`). Replace with `LinkedHashMap`, which preserves insertion order since Java 7.
- 🟠 **Remove `commons-io`** — Only used for `NullOutputStream` and `NullWriter` in 2 files (`SyntaxUtils.java`, `WriterCommandOutput.java`). Replace `NullOutputStream` with `OutputStream.nullOutputStream()` (Java 11+). Replace `NullWriter` with `Writer.nullWriter()` (Java 11+). Test-only `FileUtils` usage can use `java.nio.file.Files`.
- 🟠 **Remove `commons-text`** — Only used for `StringEscapeUtils.unescapeJava()` in 1 file (`ValueFormat.java`). Replace with a small utility method.
- 🟡 **Evaluate removing `commons-beanutils`** — Only used for `ConvertUtils.convert()` in 1 file (`SyntaxUtils.java`). Could be replaced with a custom type coercion method.
- 🟡 **Reduce `commons-lang3` surface area** — Used in ~25 files, mainly for `Validate.notNull()` (→ `Objects.requireNonNull()`), `StringUtils.isEmpty()` (→ `String.isBlank()`), `ArrayUtils.isEmpty()`, `ExceptionUtils.getMessage()`. Full removal is a larger effort but reduces transitive dependency risk.
- 🟡 **Evaluate migrating JCLI → Picocli** — JCLI (`org.cyclopsgroup:jcli:1.0.2`) is a low-activity library from ~2012. Picocli is actively maintained, has built-in completions, GraalVM support, and better documentation. The coupling is loose (annotations + `ArgumentProcessor`), making migration feasible but non-trivial.

---

## Build / POM

- 🟠 **Switch uber JAR from `maven-assembly-plugin` to `maven-shade-plugin`** — The assembly plugin's `jar-with-dependencies` unpacks all JARs naively, risking class/resource conflicts (e.g., duplicate `META-INF/services`). The shade plugin handles these via resource transformers and supports class relocation.
- 🟠 **Add `maven-enforcer-plugin` rules** — The plugin is in `pluginManagement` but has no active rules. Add: `requireMavenVersion` (≥ 3.8), `requireJavaVersion` (≥ 25), `banDuplicatePomDependencyVersions`, `dependencyConvergence`.
- 🟡 **Add static analysis plugin** — No static analysis is currently configured. Consider SpotBugs (`spotbugs-maven-plugin`), Checkstyle (`maven-checkstyle-plugin`), or ErrorProne for compile-time bug detection.
- 🟡 **Add `.editorconfig`** — Not present. Ensures consistent indentation, charset, and line endings across IDEs and editors.
- 🟡 **Add `.gitattributes`** — Not present. Normalizes line endings (`* text=auto`, `*.java text eol=lf`, `*.sh text eol=lf`) and marks binary files.

---

## GitHub Actions / CI

- 🔴 **Pin Trivy action to a release tag** — Currently `aquasecurity/trivy-action@master` in both `maven.yaml` and the Docker scan step. Using `@master` means every CI run pulls arbitrary latest code. Pin to a specific version (e.g., `@0.28.0`).
- 🟡 **Enable CodeQL scanning** — No SAST (static application security testing) is configured. Add a CodeQL workflow for Java to catch security issues in PRs.
- 🟡 **Fix nightly build schedule** — The cron expression in `nightly-build.yml` is commented out, so the workflow only runs on manual dispatch. Either un-comment or remove the workflow if nightly builds aren't needed.
- 🟡 **Remove nightly version manipulation** — The nightly workflow runs `mvn versions:set` to modify the POM version in CI. Consider using Git-based versioning (e.g., `${version}+nightly.${date}.${sha}`) without modifying source files.
- 🟢 **Verify action versions stay current** — Dependabot covers this, but worth auditing periodically. The major versions (checkout, setup-java, upload/download-artifact) should all be on their latest major.

---

## Dockerfile

- 🟠 **Run as non-root user** — The image currently runs as root. Add a dedicated `jmxterm` user and switch to it with `USER`.
- 🟡 **Add OCI labels** — Add `org.opencontainers.image.title`, `description`, `url`, `source`, `version`, `licenses` labels for registry metadata.
- 🟡 **Add `HEALTHCHECK`** — Useful for orchestration (Docker Compose, Kubernetes). A simple `java -version` check suffices.
- 🟢 **Consider multi-stage build** — Currently copies a pre-built JAR from `target/`. A multi-stage build would make the Dockerfile self-contained, though CI already handles the build step.
- 🟢 **Align base image with release target** — Dockerfile uses `eclipse-temurin:25-jre-alpine`. Ensure this matches the project's Java target version after the upgrade.

---

## Testing

- 🟡 **Evaluate migrating JMock → Mockito** — JMock 2 works but Mockito is the de facto Java mocking standard with a larger community, better IDE support, and richer assertion APIs. The test suite uses JMock's `Mockery` + `Expectations` DSL in ~15 test classes. Migration is straightforward but touches every test file.
- 🟡 **Add test coverage reporting (JaCoCo)** — No coverage tool is configured. Add `jacoco-maven-plugin` to generate reports and optionally enforce minimum coverage thresholds.
- 🟢 **Address existing TODO in `WatchCommand`** — Line 28: `TODO Consider the use case for CSV file backend generation`. Decide whether to implement or remove the comment.

---

## Documentation / Project Metadata

- 🟡 **Add `SECURITY.md`** — No vulnerability disclosure policy exists. Document how to report security issues (email, GitHub Security Advisories, etc.).
- 🟡 **Add `CONTRIBUTING.md`** — No contributor guide exists. Document build steps, testing expectations, commit conventions, and PR process.
- 🟢 **Add PR template** — `.github/PULL_REQUEST_TEMPLATE.md` helps standardize pull request descriptions.
- 🟢 **Add issue templates** — `.github/ISSUE_TEMPLATE/` with bug report and feature request templates improves issue quality.
