package org.cyclopsgroup.jmxterm.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import org.cyclopsgroup.jmxterm.cc.CommandCenter;
import org.cyclopsgroup.jmxterm.io.WriterCommandOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Integration tests for invoking MBean operations via a real JMX connection. */
class OperationInvocationIT {

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
            new javax.management.ObjectName("test:type=TestMBean"),
            "reset",
            null,
            null);
  }

  @Test
  void testRunEchoOperation() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("bean test:type=TestMBean"));
    assertTrue(cc.execute("run echo hello"));
    assertTrue(
        resultWriter.toString().contains("echo:hello"),
        "Expected 'echo:hello' in output, got: " + resultWriter);
  }

  @Test
  void testRunAddOperation() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("bean test:type=TestMBean"));
    assertTrue(cc.execute("run add 3 5"));
    assertTrue(
        resultWriter.toString().contains("8"),
        "Expected '8' in output, got: " + resultWriter);
  }

  @Test
  void testRunResetOperation() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("bean test:type=TestMBean"));
    assertTrue(cc.execute("set Name changed"));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("get Name"));
    assertTrue(
        resultWriter.toString().contains("changed"),
        "Expected 'changed' after set, got: " + resultWriter);

    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("run reset"));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("get Name"));
    assertTrue(
        resultWriter.toString().contains("default"),
        "Expected 'default' after reset, got: " + resultWriter);
  }

  @Test
  void testRunWithBeanOption() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("run -b test:type=TestMBean echo world"));
    assertTrue(
        resultWriter.toString().contains("echo:world"),
        "Expected 'echo:world' in output, got: " + resultWriter);
  }

  @Test
  void testRunNonExistentOperation() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("bean test:type=TestMBean"));
    assertFalse(cc.execute("run nonExistent"));
  }

  @Test
  void testRunWithWrongParamCount() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("bean test:type=TestMBean"));
    assertFalse(
        cc.execute("run echo too many params"),
        "Expected failure when invoking echo with wrong number of parameters");
  }
}
