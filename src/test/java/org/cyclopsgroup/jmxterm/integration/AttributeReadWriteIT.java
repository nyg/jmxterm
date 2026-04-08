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

/** Integration tests for reading and writing MBean attributes via a real JMX connection. */
class AttributeReadWriteIT {

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
  void testGetStringAttribute() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("bean test:type=TestMBean"));
    assertTrue(cc.execute("get Name"));
    assertTrue(resultWriter.toString().contains("default"));
  }

  @Test
  void testGetIntAttribute() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("bean test:type=TestMBean"));
    assertTrue(cc.execute("get Count"));
    assertTrue(resultWriter.toString().contains("0"));
  }

  @Test
  void testGetAllAttributes() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("bean test:type=TestMBean"));
    assertTrue(cc.execute("get *"));
    String result = resultWriter.toString();
    assertTrue(result.contains("Name"), "Expected output to contain 'Name', got: " + result);
    assertTrue(result.contains("Count"), "Expected output to contain 'Count', got: " + result);
  }

  @Test
  void testGetWithBeanOption() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("get -b test:type=TestMBean Name"));
    assertTrue(resultWriter.toString().contains("default"));
  }

  @Test
  void testSetAndGetAttribute() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("set -b test:type=TestMBean Name newValue"));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("get -b test:type=TestMBean Name"));
    assertTrue(
        resultWriter.toString().contains("newValue"),
        "Expected 'newValue' in output, got: " + resultWriter);
  }

  @Test
  void testSetAndVerifyCount() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("set -b test:type=TestMBean Name changed"));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("get -b test:type=TestMBean Count"));
    assertTrue(
        resultWriter.toString().contains("1"),
        "Expected Count to be 1 after one set, got: " + resultWriter);
  }

  @Test
  void testGetSimpleFormat() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("get -s -b test:type=TestMBean Name"));
    String result = resultWriter.toString().trim();
    assertTrue(
        result.contains("default"),
        "Expected simple format value 'default', got: " + result);
    assertFalse(
        result.contains("Name ="),
        "Simple format should not contain 'Name =', got: " + result);
  }

  @Test
  void testGetNonExistentAttribute() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    // GetCommand succeeds but produces no result output for unknown attributes
    assertTrue(cc.execute("get -b test:type=TestMBean NonExistent"));
    assertFalse(
        resultWriter.toString().contains("NonExistent"),
        "Expected no result for non-existent attribute, got: " + resultWriter);
  }
}
