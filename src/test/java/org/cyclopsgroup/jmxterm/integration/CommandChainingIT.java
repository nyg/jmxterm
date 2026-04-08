package org.cyclopsgroup.jmxterm.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import org.cyclopsgroup.jmxterm.cc.CommandCenter;
import org.cyclopsgroup.jmxterm.io.WriterCommandOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Integration tests for command chaining, comments, and empty commands. */
class CommandChainingIT {

  @RegisterExtension static EmbeddedJmxServer jmxServer = new EmbeddedJmxServer();

  private CommandCenter cc;
  private StringWriter resultWriter;
  private StringWriter messageWriter;

  @BeforeEach
  void setUp() throws Exception {
    resetMBeanState();
    resultWriter = new StringWriter();
    messageWriter = new StringWriter();
    cc = new CommandCenter(new WriterCommandOutput(resultWriter, messageWriter), null);
  }

  @AfterEach
  void tearDown() throws Exception {
    cc.close();
    resetMBeanState();
  }

  private void resetMBeanState() throws Exception {
    jmxServer
        .getMBeanServer()
        .invoke(
            new javax.management.ObjectName("test:type=TestMBean"), "reset", null, null);
  }

  @Test
  void testCommandChaining() {
    assertTrue(
        cc.execute(
            "open " + jmxServer.getConnectionUrl() + " && domains && beans -d test"));
    String result = resultWriter.toString();
    assertTrue(result.contains("test"), "Expected 'test' domain in output, got: " + result);
    assertTrue(
        result.contains("JMImplementation"),
        "Expected 'JMImplementation' domain in output, got: " + result);
    assertTrue(
        result.contains("test:type=TestMBean"),
        "Expected 'test:type=TestMBean' in output, got: " + result);
  }

  @Test
  void testCommentHandling() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("domains # this is a comment"));
    String result = resultWriter.toString();
    assertTrue(result.contains("test"), "Expected domains output, got: " + result);
  }

  @Test
  void testFullLineComment() {
    assertTrue(cc.execute("# this is a comment"));
    assertEquals("", resultWriter.toString(), "Full-line comment should produce no output");
  }

  @Test
  void testEscapedHash() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("set -b test:type=TestMBean Name value\\#with\\#hashes"));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("get -b test:type=TestMBean Name"));
    String result = resultWriter.toString();
    assertTrue(
        result.contains("value#with#hashes"),
        "Expected value with unescaped hashes, got: " + result);
  }

  @Test
  void testEmptyCommand() {
    assertTrue(cc.execute(""), "Empty command should succeed");
    assertEquals("", resultWriter.toString(), "Empty command should produce no output");
  }
}
