# Build Process

This document describes what happens when you run `mvn clean install` and the artifacts produced at
each stage.

## Quick Reference

```bash
# Full build (clean + compile + test + package + integration tests + install)
mvn clean install

# Compile only
mvn compile

# Run unit tests
mvn test

# Package artifacts (JAR, uber JAR, DEB, RPM)
mvn package

# Full validation including integration and E2E tests
mvn verify
```

## Build Phases

### 1. Clean

Deletes the `target/` directory and all previously built artifacts.

### 2. Compile

**Plugin:** `maven-compiler-plugin`

Compiles all Java sources under `src/main/java` using Java 17 language level. The `--release 17`
flag ensures the compiled bytecode is compatible with Java 17 and later.

### 3. Test (unit tests)

**Plugin:** `maven-surefire-plugin`

Runs unit tests under `src/test/java`. These tests use JUnit 5 and JMock 2 to verify individual
command behavior against mocked `MBeanServerConnection` instances.

> Tests in the `org.cyclopsgroup.jmxterm.jdk*` package are excluded because they depend on
> platform-specific JVM attach APIs.

### 4. Package

Several plugins run during the package phase to produce the distribution artifacts:

#### 4a. JAR

**Plugin:** `maven-jar-plugin`

Produces the standard library JAR (`jmxsh-<version>.jar`) containing only the project's compiled
classes.

#### 4b. Uber JAR

**Plugin:** `maven-assembly-plugin`

Produces a self-contained uber JAR (`jmxsh-<version>-uber.jar`) that bundles all runtime
dependencies into a single executable file. The manifest sets
`org.cyclopsgroup.jmxterm.boot.CliMain` as the main class, so it can be run directly:

```bash
java -jar jmxsh-<version>-uber.jar
```

#### 4c. Debian Package

**Plugin:** `jdeb`

Produces a `.deb` package for Debian/Ubuntu systems. Installs the uber JAR to
`/usr/share/jmxsh/jmxsh-uber.jar` and a launch script to `/usr/bin/jmxsh`.

#### 4d. RPM Package

**Plugin:** `rpm-maven-plugin`

Produces an `.rpm` package for Red Hat/CentOS/Fedora systems with the same installation layout as
the DEB package.

#### 4e. Source JAR

**Plugin:** `maven-source-plugin`

Attaches a source JAR (`jmxsh-<version>-sources.jar`) for distribution alongside the binary.

### 5. Verify (integration + E2E tests)

**Plugin:** `maven-failsafe-plugin`

Runs integration and end-to-end tests (files matching `*IT.java`):

- **Integration tests** (`src/test/java/.../integration/`) — exercise commands against an embedded
  JMX server running in the same JVM
- **E2E tests** (`src/test/java/.../e2e/`) — launch the uber JAR as a subprocess and communicate
  with a separate target JVM over JMX/RMI

### 6. Install

Copies all artifacts (JARs, DEB, RPM) into the local Maven repository (`~/.m2/repository`) so other
local projects can reference them as dependencies.

## Artifacts Summary

| Artifact | Location | Description |
|---|---|---|
| `jmxsh-<v>.jar` | `target/` | Library JAR (classes only) |
| `jmxsh-<v>-uber.jar` | `target/` | Executable uber JAR (all deps bundled) |
| `jmxsh-<v>.deb` | `target/` | Debian package |
| `jmxsh-<v>.rpm` | `target/rpm/` | RPM package |
| `jmxsh-<v>-sources.jar` | `target/` | Source code JAR |

## CI/CD

The CI workflow (`maven.yaml`) runs `mvn verify` (compile + unit + integration + E2E tests) on
every push and pull request targeting `master`.

The release workflow (`create-release.yaml`) runs on tag pushes (`v*.*.*`) and handles:

1. **Build & Scan** — `mvn package` on JDK 25, then Trivy vulnerability scanning (filesystem)
2. **Docker** — builds and scans a multi-architecture Docker image (`linux/amd64`, `linux/arm64`),
   then pushes to `ghcr.io/nyg/jmxsh`
3. **GitHub Release** — creates a release with JARs, DEB packages, and auto-generated changelog
