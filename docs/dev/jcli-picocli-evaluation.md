# JCLI → Picocli Migration Evaluation

This document evaluates migrating from
[JCLI](https://github.com/jiaqi/jcli) (`org.cyclopsgroup:jcli:1.0.2`) to
[Picocli](https://picocli.info/) (`info.picocli:picocli`) for command-line argument parsing.

## Current State

jmxsh uses JCLI annotations across ~24 command classes, the `Command` base class, and
`CliMainOptions`. JCLI brings a transitive dependency on `caff` (`org.cyclopsgroup:caff:0.4.2`),
which provides the `EscapingValueTokenizer` used in `CommandCenter` for command-line tokenization.

| Component | Current Library |
|---|---|
| `@Cli`, `@Option`, `@Argument`, `@MultiValue` annotations | jcli |
| `ArgumentProcessor.forType()` / `ap.process()` | jcli |
| `AutoCompletable` interface | jcli |
| `EscapingValueTokenizer` | caff (transitive via jcli) |

## Pros

### User Experience

- **Colored help output** — Picocli renders help text with ANSI colors by default. Current jmxsh
  help is plain monochrome text. This significantly improves readability in modern terminals.
- **Better error messages** — Picocli provides "Did you mean...?" suggestions for misspelled
  commands and options, and clear messages for missing required arguments.
- **Built-in `--version` option** — Currently not available; would need custom implementation
  with JCLI.
- **Tab completion generation** — Picocli generates shell completion scripts for bash, zsh, and
  fish. jmxsh currently has a custom `ConsoleCompletor` implementation; picocli's `ICompleter`
  integration with JLine could simplify this.
- **Subcommand support** — First-class support for git-style nested subcommands. While jmxsh uses
  a flat command model today, this opens future possibilities.

### Speed

- **Compile-time annotation processing** — Picocli's `picocli-codegen` module generates reflection
  configuration at compile time, avoiding runtime reflection overhead. JCLI uses runtime reflection
  via `ArgumentProcessor.forType()`.
- **Practical impact: negligible** — Argument parsing takes <1ms either way for a CLI tool. The
  difference is architectural (cleaner) rather than measurable.

### Jar Size

| Library | Size |
|---|---|
| jcli-1.0.2.jar | 47 KB |
| caff-0.4.2.jar | 86 KB |
| **JCLI total** | **133 KB** |
| picocli-4.7.7.jar | ~330 KB |
| **Net increase** | **~197 KB** |

The uber JAR is currently 6.2 MB, so this represents a **~3% increase** — negligible.

Picocli would also let us **remove the `caff` dependency entirely**: the
`EscapingValueTokenizer` in `CommandCenter` can be replaced with picocli's built-in parsing or a
simple custom tokenizer.

### Maintainability

- **Active maintenance** — Picocli is actively developed (latest release: 2025). JCLI's last
  meaningful update was in 2012. Depending on an abandoned library is a long-term maintenance risk.
- **Documentation** — Picocli has extensive documentation, a user manual, javadoc, and active
  community support (Stack Overflow, GitHub issues).
- **Expressive annotations** — Picocli's `@Command`, `@Parameters`, `@Option` support `arity`,
  `converter`, `completionCandidates`, `interactive` (for password prompts), and more. These are
  not available in JCLI.
- **GraalVM native-image support** — Picocli has first-class support for compiling to native
  binaries via GraalVM. This opens future possibilities for instant startup time.

## Cons

### User Experience

- **Help format change** — Picocli's default help layout differs from JCLI's. Existing users
  familiar with the current help output will notice the change. This can be customized but requires
  extra work.

### Migration Effort

The coupling is loose (annotations + `ArgumentProcessor`), but the migration scope is non-trivial:

| Component | Files | Effort |
|---|---|---|
| Command annotations (`@Cli`, `@Option`, `@Argument`, `@MultiValue`) | ~24 command classes | Straightforward 1:1 mapping |
| `Command` base class | 1 | Moderate — `AutoCompletable` interface changes |
| `CliMainOptions` | 1 | Straightforward |
| `CommandCenter` (argument parsing) | 1 | Moderate — replace `ArgumentProcessor` with picocli parsing |
| `ConsoleCompletor` (tab completion) | 1 | Significant — different completion model |
| `HelpCommand` | 1 | Moderate — picocli has built-in help rendering |
| `EscapingValueTokenizer` (from caff) | 1 | Need alternative tokenizer or use picocli's parsing |
| Test helpers (`SelfRecordingCommand`) | 1 | Straightforward |

Estimated effort: **2–3 days** for a developer familiar with both libraries.

### Jar Size

Small net increase (~197 KB / 3%).

### Risk

- **Regression in argument parsing** — JCLI and picocli may handle edge cases differently
  (escaped characters, multi-value arguments, whitespace handling). Thorough testing is required.
- **The `caff` `EscapingValueTokenizer`** handles specific escaping patterns for the jmxsh
  command syntax (e.g., `#` comments, `&&` chaining). This logic is in `CommandCenter` and may
  need to be preserved independently of the argument parser.

## Recommendation

**Migrate.** The long-term maintenance risk of depending on an abandoned 2012 library outweighs
the one-time migration effort. The UX improvements (colored help, better error messages, tab
completion scripts) directly benefit users. The jar size increase is negligible.

### Migration Strategy

The migration was implemented in a single pass. See details below.

## Implementation

The migration is complete. All JCLI annotations, parsing infrastructure, and tab-completion
integration have been replaced with Picocli equivalents.

### Dependency Changes

| Action | Artifact | Version |
|---|---|---|
| Removed | `org.cyclopsgroup:jcli` | 1.0.2 |
| Added | `info.picocli:picocli` | 4.7.7 |
| Added (was transitive) | `org.cyclopsgroup:caff` | 0.4.2 |

The `caff` dependency was promoted to a direct dependency because `EscapingValueTokenizer` is used
in `CommandCenter` for jmxsh-specific tokenization (escaped characters, `#` comments, `&&`
chaining). This tokenization layer is independent of argument parsing and must be preserved.

### Annotation Mapping Applied

| JCLI | Picocli |
|---|---|
| `@Cli(name, description, note)` | `@Command(name, description, footer)` |
| `@Option(name="x", longName="y", description)` | `@Option(names={"-x","--y"}, description)` |
| `@Option(displayName="n")` | `@Option(paramLabel="n")` |
| `@Option(defaultValue="v")` | `@Option(defaultValue="v")` |
| `@Argument(displayName, description)` | `@Parameters(paramLabel, description, arity)` |
| `@MultiValue(listType=ArrayList.class, minValues=N)` + `@Argument` | `@Parameters(arity="N..*")` |
| `ArgumentProcessor.forType(cls).process(args, obj)` | `new CommandLine(obj).parseArgs(args)` |

### Files Changed

| Component | Files | Notes |
|---|---|---|
| Dependencies | `pom.xml` | Swapped jcli → picocli + caff |
| Base class | `Command.java` | Replaced `AutoCompletable` with custom `Completable` interface |
| New interface | `Completable.java` | Replaces jcli's `AutoCompletable` |
| Boot | `CliMainOptions.java`, `CliMain.java` | Migrated annotations and parsing |
| Execution | `CommandCenter.java` | `ArgumentProcessor` → `CommandLine`, added `setUnmatchedOptionsArePositionalParams(true)` |
| Help | `HelpCommand.java` | Full rewrite using picocli `CommandSpec` |
| Completion | `ConsoleCompletor.java` | Full rewrite using picocli `CommandSpec` for option/argument completion |
| Commands | 17 classes in `cmd/` | All annotations migrated |
| Tests | `SelfRecordingCommand.java`, `HelpCommandTest.java` | Updated for picocli annotations |

### Key Decisions

1. **`@picocli.CommandLine.Command` (fully qualified)** — Used in all `cmd/` classes to avoid a
   name clash between picocli's `@Command` annotation and the project's `Command` base class.

2. **`setUnmatchedOptionsArePositionalParams(true)`** — Required because picocli, unlike JCLI,
   rejects unknown tokens starting with `-`. This setting restores JCLI's lenient behavior where
   values like `-3` are treated as positional arguments.

3. **`arity="0..1"` for optional single-value parameters** — JCLI implicitly allowed 0 or 1
   arguments for `String` setters. Picocli defaults `@Parameters` on `String` to arity `"1"`
   (required). Explicit `arity="0..1"` was set on `BeanCommand.setBean()`,
   `DomainCommand.setDomain()`, and `OpenCommand.setUrl()`.

4. **`caff` retained as direct dependency** — `EscapingValueTokenizer` handles jmxsh-specific
   tokenization that is orthogonal to argument parsing.
