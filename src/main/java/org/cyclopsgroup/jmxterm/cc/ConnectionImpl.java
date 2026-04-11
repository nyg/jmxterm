package org.cyclopsgroup.jmxterm.cc;

import java.io.IOException;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.util.Objects;
import org.cyclopsgroup.jmxterm.Connection;

/**
 * Identifies a JMX connection
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
class ConnectionImpl implements Connection {
  private final JMXConnector connector;

  private final JMXServiceURL url;

  /**
   * @param connector JMX connector
   * @param url JMX service URL object
   */
  ConnectionImpl(JMXConnector connector, JMXServiceURL url) {
    Objects.requireNonNull(connector, "JMX connector can't be NULL");
    Objects.requireNonNull(url, "JMX service URL can't be NULL");
    this.connector = connector;
    this.url = url;
  }

  /**
   * Close current connection
   *
   * @throws IOException Communication error
   */
  void close() throws IOException {
    connector.close();
  }

  /** @return JMX connector */
  public final JMXConnector getConnector() {
    return connector;
  }

  @Override
  public String getConnectorId() throws IOException {
    return connector.getConnectionId();
  }

  @Override
  public MBeanServerConnection getServerConnection() throws IOException {
    return connector.getMBeanServerConnection();
  }

  @Override
  public final JMXServiceURL getUrl() {
    return url;
  }

  /**
   * Check if the connection is still alive by performing a lightweight call.
   *
   * @return True if the connection is responsive, false if it appears broken
   */
  boolean isAlive() {
    try {
      connector.getConnectionId();
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
