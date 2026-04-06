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

/** Integration tests for error handling in various failure scenarios. */
class ErrorHandlingIT {

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
  void testGetWithoutConnection() {
    assertFalse(
        cc.execute("get Name"),
        "Expected failure when getting attribute without a connection");
  }

  @Test
  void testGetWithoutBean() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertFalse(
        cc.execute("get Name"), "Expected failure when getting attribute without selecting a bean");
  }

  @Test
  void testInvalidBeanName() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertFalse(
        cc.execute("bean invalid:name:format:::"),
        "Expected failure for malformed bean name");
  }

  @Test
  void testConnectToBadPort() {
    assertFalse(
        cc.execute("open localhost:1"),
        "Expected failure when connecting to an invalid port");
  }

  @Test
  void testDomainWithoutConnection() {
    assertFalse(
        cc.execute("domains"), "Expected failure when listing domains without a connection");
  }

  @Test
  void testSetReadOnlyAttribute() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertFalse(
        cc.execute("set -b test:type=TestMBean Count 5"),
        "Expected failure when setting read-only attribute Count");
  }
}
