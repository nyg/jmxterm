package org.cyclopsgroup.jmxterm.integration;

import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that starts an embedded JMX connector server for integration testing. The
 * server listens on a random available port and registers a {@link TestMBeanImpl} under the {@code
 * test:type=TestMBean} ObjectName.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * @RegisterExtension
 * static EmbeddedJmxServer jmxServer = new EmbeddedJmxServer();
 * }</pre>
 */
public class EmbeddedJmxServer implements BeforeAllCallback, AfterAllCallback {

  private MBeanServer mBeanServer;
  private JMXConnectorServer connectorServer;
  private Registry registry;
  private int port;

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    // Find a free port
    try (ServerSocket ss = new ServerSocket(0)) {
      port = ss.getLocalPort();
    }

    // Create a dedicated MBeanServer (not the platform one, to avoid side effects)
    mBeanServer = MBeanServerFactory.createMBeanServer("test-" + port);

    // Register test MBean
    mBeanServer.registerMBean(
        new StandardMBean(new TestMBeanImpl(), TestMBean.class),
        new ObjectName("test:type=TestMBean"));

    // Create RMI registry on the chosen port
    registry = LocateRegistry.createRegistry(port);

    // Start JMX connector server
    JMXServiceURL url =
        new JMXServiceURL(
            "service:jmx:rmi:///jndi/rmi://localhost:" + port + "/jmxrmi");
    connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mBeanServer);
    connectorServer.start();
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    if (connectorServer != null) {
      connectorServer.stop();
    }
    if (registry != null) {
      UnicastRemoteObject.unexportObject(registry, true);
    }
    if (mBeanServer != null) {
      MBeanServerFactory.releaseMBeanServer(mBeanServer);
    }
  }

  /** @return The port the JMX connector server is listening on */
  public int getPort() {
    return port;
  }

  /** @return A connection URL in {@code localhost:PORT} format suitable for the open command */
  public String getConnectionUrl() {
    return "localhost:" + port;
  }

  /** @return The full JMX service URL string */
  public String getServiceUrl() {
    return "service:jmx:rmi:///jndi/rmi://localhost:" + port + "/jmxrmi";
  }

  /** @return The underlying MBeanServer for direct manipulation in tests */
  public MBeanServer getMBeanServer() {
    return mBeanServer;
  }
}
