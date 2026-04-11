package org.cyclopsgroup.jmxterm.cc;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.cyclopsgroup.jmxterm.Connection;
import org.cyclopsgroup.jmxterm.JavaProcessManager;
import org.cyclopsgroup.jmxterm.Session;
import org.cyclopsgroup.jmxterm.io.CommandInput;
import org.cyclopsgroup.jmxterm.io.CommandOutput;

/**
 * Implementation of {@link Session} which keeps a {@link ConnectionImpl}
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
class SessionImpl extends Session {
  static final int DEFAULT_RETRY_INTERVAL_SECONDS = 5;
  static final int DEFAULT_MAX_RETRY_ATTEMPTS = 12;

  private ConnectionImpl connection;
  private JMXServiceURL lastUrl;
  private Map<String, Object> lastEnv;

  /**
   * @param output Output result
   * @param input Command line input
   * @param jpm Java process manager
   */
  SessionImpl(CommandOutput output, CommandInput input, JavaProcessManager jpm) {
    super(output, input, jpm);
  }

  @Override
  public void connect(JMXServiceURL url, Map<String, Object> env) throws IOException {
    Objects.requireNonNull(url, "URL can't be NULL");
    if (connection != null) {
      throw new IllegalStateException("Session is already opened");
    }
    JMXConnector connector = doConnect(url, env);
    connection = new ConnectionImpl(connector, url);
    lastUrl = url;
    lastEnv = env;
  }

  @Override
  public void disconnect() throws IOException {
    closeConnection();
    lastUrl = null;
    lastEnv = null;
    unsetDomain();
  }

  /**
   * Close the current connection without clearing reconnection state (lastUrl, lastEnv) or
   * domain/bean selection. Used internally during reconnection attempts.
   */
  private void closeConnection() {
    if (connection == null) {
      return;
    }
    try {
      connection.close();
    } catch (IOException e) {
      // Connection is already broken — force cleanup
    } finally {
      connection = null;
    }
  }

  /**
   * Connect to MBean server
   *
   * @param url MBean server URL
   * @param env A map of environment
   * @return Connector that holds connection to MBean server
   * @throws IOException Network errors
   */
  protected JMXConnector doConnect(JMXServiceURL url, Map<String, Object> env) throws IOException {
    return JMXConnectorFactory.connect(url, env);
  }

  @Override
  public Connection getConnection() {
    if (connection == null) {
      throw new IllegalStateException(
          "Connection isn't open yet. Run open command to open a connection");
    }
    return connection;
  }

  @Override
  public boolean isConnected() {
    return connection != null;
  }

  /**
   * @return True if this session has enough information to attempt a reconnect (i.e., a previous
   *     connection was established and the URL was stored).
   */
  boolean canReconnect() {
    return lastUrl != null;
  }

  /**
   * Check if the current connection is alive by performing a lightweight RMI call.
   *
   * @return True if the connection is alive, false if broken or not connected
   */
  boolean isConnectionAlive() {
    if (connection == null) {
      return false;
    }
    return connection.isAlive();
  }

  /**
   * Attempt to reconnect to the last known JMX endpoint. Closes the current (broken) connection
   * without clearing domain/bean selection, then retries every {@code intervalSeconds} seconds up to
   * {@code maxAttempts} times. On failure, performs a full disconnect (clearing all state).
   *
   * @param intervalSeconds Seconds to wait between retry attempts
   * @param maxAttempts Maximum number of reconnection attempts
   * @return True if reconnection was successful
   */
  boolean reconnect(int intervalSeconds, int maxAttempts) {
    closeConnection();

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      output.printMessage(
          "Reconnection attempt %d/%d in %d seconds..."
              .formatted(attempt, maxAttempts, intervalSeconds));
      try {
        Thread.sleep(intervalSeconds * 1000L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        output.printMessage("Reconnection interrupted.");
        fullDisconnect();
        return false;
      }
      try {
        JMXConnector connector = doConnect(lastUrl, lastEnv);
        connection = new ConnectionImpl(connector, lastUrl);
        return true;
      } catch (IOException e) {
        // Will retry
      }
    }
    output.printMessage("All reconnection attempts failed.");
    fullDisconnect();
    return false;
  }

  /** Clear all connection state including reconnection parameters and domain/bean. */
  private void fullDisconnect() {
    closeConnection();
    lastUrl = null;
    lastEnv = null;
    unsetDomain();
  }
}
