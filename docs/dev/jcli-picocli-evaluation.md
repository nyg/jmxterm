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

### Suggested Migration Strategy

1. **Phase 1: Add picocli dependency alongside JCLI** — Migrate `CliMainOptions` first as a
   proof of concept.
2. **Phase 2: Migrate command annotations** — Convert commands one by one, starting with simple
   ones (e.g., `QuitCommand`, `CloseCommand`) and progressing to complex ones (`RunCommand`,
   `WatchCommand`).
3. **Phase 3: Replace `ArgumentProcessor` in `CommandCenter`** — Switch the command execution
   pipeline to picocli.
4. **Phase 4: Rework tab completion** — Replace `ConsoleCompletor` with picocli's JLine
   integration.
5. **Phase 5: Remove JCLI and caff** — Clean up the old dependencies.
