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

/** Integration tests for JMX connection lifecycle: open, close, reconnect. */
class ConnectionLifecycleIT {

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
  void testOpenConnection() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("close"));
  }

  @Test
  void testOpenDisplaysConnectionInfo() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("open"));
    String result = resultWriter.toString();
    assertTrue(
        result.contains(jmxServer.getServiceUrl()),
        "Expected service URL in output, got: " + result);
  }

  @Test
  void testCloseAndReconnect() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertTrue(cc.execute("close"));
    assertTrue(
        cc.execute("open " + jmxServer.getConnectionUrl()),
        "Reconnection after close should succeed");
    // Verify the connection works by running a command
    assertTrue(cc.execute("domains"));
  }

  @Test
  void testOpenWhenAlreadyConnected() {
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
    assertFalse(
        cc.execute("open " + jmxServer.getConnectionUrl()),
        "Opening a second connection without closing should fail");
  }

  @Test
  void testCloseWithoutOpen() {
    assertTrue(
        cc.execute("close"),
        "Closing without an open connection should succeed (no-op disconnect)");
  }

  @Test
  void testOpenWithFullServiceUrl() {
    assertTrue(cc.execute("open " + jmxServer.getServiceUrl()));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("open"));
    String result = resultWriter.toString();
    assertTrue(
        result.contains(jmxServer.getServiceUrl()),
        "Expected service URL in output, got: " + result);
  }
}
