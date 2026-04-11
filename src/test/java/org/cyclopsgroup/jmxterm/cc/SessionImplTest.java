package org.cyclopsgroup.jmxterm.cc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.cyclopsgroup.jmxterm.Connection;
import org.cyclopsgroup.jmxterm.SyntaxUtils;
import org.cyclopsgroup.jmxterm.io.WriterCommandOutput;
import org.cyclopsgroup.jmxterm.jdk9.Jdk9JavaProcessManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case of {@link SessionImpl}
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
class SessionImplTest {
  private JMXConnector con;

  private SessionImpl session;

  /** Set up objects to test */
  @BeforeEach
  void setUp() {
    con = mock(JMXConnector.class);
    session =
        new SessionImpl(
            new WriterCommandOutput(Writer.nullWriter()),
            null,
            new Jdk9JavaProcessManager()) {
          @Override
          protected JMXConnector doConnect(JMXServiceURL url, Map<String, Object> env)
              throws IOException {
            return con;
          }
        };
  }

  /**
   * Verify connect() runs correctly
   *
   * @throws IOException
   */
  @Test
  void connect() throws Exception {
    session.connect(SyntaxUtils.getUrl("localhost:9991", null), null);
    Connection conn = session.getConnection();
    assertThat(conn.getUrl().toString())
        .isEqualTo("service:jmx:rmi:///jndi/rmi://localhost:9991/jmxrmi");
  }

  @Test
  void connectStoresReconnectionParameters() throws Exception {
    assertThat(session.canReconnect()).isFalse();
    session.connect(SyntaxUtils.getUrl("localhost:9991", null), null);
    assertThat(session.canReconnect()).isTrue();
  }

  @Test
  void disconnectClearsReconnectionParameters() throws Exception {
    session.connect(SyntaxUtils.getUrl("localhost:9991", null), null);
    session.disconnect();
    assertThat(session.canReconnect()).isFalse();
    assertThat(session.isConnected()).isFalse();
  }

  @Test
  void disconnectClearsDomainAndBean() throws Exception {
    session.connect(SyntaxUtils.getUrl("localhost:9991", null), null);
    session.setDomain("java.lang");
    session.setBean("java.lang:type=Runtime");
    session.disconnect();
    assertThat(session.getDomain()).isNull();
    assertThat(session.getBean()).isNull();
  }

  @Test
  void isConnectionAliveWhenConnected() throws Exception {
    when(con.getConnectionId()).thenReturn("test-id");
    session.connect(SyntaxUtils.getUrl("localhost:9991", null), null);
    assertThat(session.isConnectionAlive()).isTrue();
  }

  @Test
  void isConnectionAliveWhenBroken() throws Exception {
    when(con.getConnectionId()).thenThrow(new IOException("broken"));
    session.connect(SyntaxUtils.getUrl("localhost:9991", null), null);
    assertThat(session.isConnectionAlive()).isFalse();
  }

  @Test
  void isConnectionAliveWhenNotConnected() {
    assertThat(session.isConnectionAlive()).isFalse();
  }

  @Test
  void reconnectSucceedsOnFirstAttempt() throws Exception {
    session.connect(SyntaxUtils.getUrl("localhost:9991", null), null);
    session.setDomain("java.lang");
    session.setBean("java.lang:type=Runtime");

    boolean result = session.reconnect(0, 1);

    assertThat(result).isTrue();
    assertThat(session.isConnected()).isTrue();
    // Domain and bean are preserved across reconnect
    assertThat(session.getDomain()).isEqualTo("java.lang");
    assertThat(session.getBean()).isEqualTo("java.lang:type=Runtime");
  }

  @Test
  void reconnectFailsAfterMaxAttempts() throws Exception {
    AtomicInteger connectCount = new AtomicInteger(0);
    var reconnectSession =
        new SessionImpl(
            new WriterCommandOutput(Writer.nullWriter()),
            null,
            new Jdk9JavaProcessManager()) {
          @Override
          protected JMXConnector doConnect(JMXServiceURL url, Map<String, Object> env)
              throws IOException {
            if (connectCount.getAndIncrement() == 0) {
              return con; // initial connect succeeds
            }
            throw new IOException("Connection refused");
          }
        };

    reconnectSession.connect(SyntaxUtils.getUrl("localhost:9991", null), null);
    assertThat(reconnectSession.canReconnect()).isTrue();

    boolean result = reconnectSession.reconnect(0, 2);
    assertThat(result).isFalse();
    assertThat(reconnectSession.isConnected()).isFalse();
    assertThat(reconnectSession.canReconnect()).isFalse();
  }

  @Test
  void reconnectSucceedsAfterTransientFailure() throws Exception {
    AtomicInteger connectCount = new AtomicInteger(0);
    JMXConnector freshCon = mock(JMXConnector.class);

    var reconnectSession =
        new SessionImpl(
            new WriterCommandOutput(Writer.nullWriter()),
            null,
            new Jdk9JavaProcessManager()) {
          @Override
          protected JMXConnector doConnect(JMXServiceURL url, Map<String, Object> env)
              throws IOException {
            int count = connectCount.getAndIncrement();
            if (count == 0) {
              return con; // initial connect succeeds
            } else if (count == 1) {
              throw new IOException("Transient failure"); // first reconnect fails
            }
            return freshCon; // second reconnect succeeds
          }
        };

    reconnectSession.connect(SyntaxUtils.getUrl("localhost:9991", null), null);
    boolean result = reconnectSession.reconnect(0, 3);

    assertThat(result).isTrue();
    assertThat(reconnectSession.isConnected()).isTrue();
    // canReconnect is still true (lastUrl preserved)
    assertThat(reconnectSession.canReconnect()).isTrue();
  }
}
