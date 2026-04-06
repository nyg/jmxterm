package org.cyclopsgroup.jmxterm.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import org.cyclopsgroup.jmxterm.cc.CommandCenter;
import org.cyclopsgroup.jmxterm.io.VerboseLevel;
import org.cyclopsgroup.jmxterm.io.WriterCommandOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Integration tests for verbose level filtering of output and error messages. */
class VerboseLevelIT {

  @RegisterExtension static EmbeddedJmxServer jmxServer = new EmbeddedJmxServer();

  private CommandCenter cc;
  private StringWriter resultWriter;
  private StringWriter messageWriter;

  @BeforeEach
  void setUp() throws Exception {
    resultWriter = new StringWriter();
    messageWriter = new StringWriter();
    cc = new CommandCenter(new WriterCommandOutput(resultWriter, messageWriter), null);
  }

  @AfterEach
  void tearDown() {
    cc.close();
  }

  @Test
  void testBriefMessages() {
    // Default level is BRIEF — messages should appear with '#' prefix
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    String messages = messageWriter.toString();
    assertTrue(
        messages.contains("#"),
        "Expected '#' prefixed messages in BRIEF mode, got: " + messages);
    assertTrue(
        messages.contains("Connection to"),
        "Expected connection message, got: " + messages);
  }

  @Test
  void testSilentSuppressesMessages() {
    cc.setVerboseLevel(VerboseLevel.SILENT);
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("bean test:type=TestMBean"));
    assertTrue(cc.execute("get Name"));
    String messages = messageWriter.toString();
    assertTrue(messages.isEmpty(), "Expected no messages in SILENT mode, got: " + messages);
  }

  @Test
  void testSilentStillShowsValues() {
    cc.setVerboseLevel(VerboseLevel.SILENT);
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("bean test:type=TestMBean"));
    assertTrue(cc.execute("get Name"));
    assertTrue(
        resultWriter.toString().contains("default"),
        "Expected result value 'default' even in SILENT mode, got: " + resultWriter);
  }

  @Test
  void testVerboseShowsStackTraces() {
    cc.setVerboseLevel(VerboseLevel.VERBOSE);
    // Execute a command that will fail (get attribute without connection)
    assertFalse(cc.execute("get Name"));
    String messages = messageWriter.toString();
    assertTrue(
        messages.contains("at "),
        "Expected stack trace lines in VERBOSE mode, got: " + messages);
  }

  @Test
  void testBriefShowsShortErrors() {
    // Default level is BRIEF
    assertFalse(cc.execute("get Name"));
    String messages = messageWriter.toString();
    assertTrue(
        messages.contains("#"),
        "Expected '#' prefixed error in BRIEF mode, got: " + messages);
    assertFalse(
        messages.contains("\tat "),
        "Expected no full stack trace in BRIEF mode, got: " + messages);
  }
}
