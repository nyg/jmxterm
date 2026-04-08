# JMXTerm

> Interactive command-line JMX client for monitoring and managing Java applications.

JMXTerm lets you connect to any JMX-enabled JVM, browse MBeans, read and write attributes, and
invoke operations — all from the comfort of your terminal. Think of it as a Swiss Army knife for JMX.

## Quick Start

Download the latest [uber JAR from Releases](https://github.com/nyg/jmxterm/releases) and run:

```bash
java -jar jmxterm-uber.jar
```

Or use Docker:

```bash
docker run -it ghcr.io/lemyst/jmxterm
```

## Usage

```
$ java -jar jmxterm-uber.jar
Welcome to JMX terminal. Type "help" for available commands.
$> open localhost:9999
#Connection to localhost:9999 is opened
$> domains
#following domains are available
JMImplementation
java.lang
com.example
$> bean com.example:type=AppStats
#bean is set to com.example:type=AppStats
$> get RequestCount
#mbean = com.example:type=AppStats:
RequestCount = 42;
$> run resetStats
#calling operation resetStats of mbean com.example:type=AppStats
#operation returns:
null
$> close
$> quit
```

### Key Commands

| Command | Description |
|---|---|
| `open <host:port>` | Connect to a remote JMX endpoint |
| `open <pid>` | Attach to a local JVM by process ID |
| `domains` | List all MBean domains |
| `beans` | List all MBeans (optionally filter by domain with `-d`) |
| `bean <name>` | Select an MBean for subsequent operations |
| `info` | Show attributes and operations of the selected MBean |
| `get <attr>` | Read an MBean attribute |
| `set <attr> <value>` | Write an MBean attribute |
| `run <op> [args]` | Invoke an MBean operation |
| `close` | Disconnect from the JMX endpoint |
| `jvms` | List local Java processes |
| `help` | Show all available commands |

### Non-Interactive Mode

Run commands from a script file:

```bash
java -jar jmxterm-uber.jar -l localhost:9999 --input commands.txt
```

Or pipe commands via stdin:

```bash
echo "open localhost:9999 && beans" | java -jar jmxterm-uber.jar -n
```

## Installation

### JAR (all platforms)

```bash
java -jar jmxterm-uber.jar
```

### Docker

```bash
docker run -it ghcr.io/lemyst/jmxterm
```

### Debian/Ubuntu

```bash
sudo dpkg -i jmxterm-<version>.deb
jmxterm
```

### Maven Dependency

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/LeMyst/jmxterm</url>
    </repository>
</repositories>
<dependency>
    <groupId>io.github.lemyst</groupId>
    <artifactId>jmxterm</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Features

- **Interactive REPL** with tab completion and command history (JLine)
- **Remote & local connections** — connect via host:port, JMX URL, or local PID
- **Full MBean support** — browse domains, read/write attributes, invoke operations
- **Command chaining** — run multiple commands in one line with `&&`
- **Script mode** — automate JMX operations via files or piped input
- **Verbose control** — silent, brief, or verbose output modes
- **Cross-platform** — runs anywhere Java runs (JAR, Docker, DEB, RPM)

## Documentation

- [Architecture](docs/architecture.md)
- [Build Process](docs/build-process.md)

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.

