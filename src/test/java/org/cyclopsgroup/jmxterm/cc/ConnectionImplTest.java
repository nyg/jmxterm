package org.cyclopsgroup.jmxterm.cc;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import javax.management.remote.JMXConnector;
import org.cyclopsgroup.jmxterm.SyntaxUtils;
import org.junit.jupiter.api.Test;

/**
 * Test case of {@link ConnectionImpl}
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
class ConnectionImplTest {
  /**
   * Test the object is constructed correctly
   *
   * @throws IOException
   */
  @Test
  void construction() throws Exception {
    JMXConnector con = mock(JMXConnector.class);
    ConnectionImpl c = new ConnectionImpl(con, SyntaxUtils.getUrl("localhost:9991", null));
    assertThat(c.getConnector()).isSameAs(con);

    when(con.getConnectionId()).thenReturn("xyz");
    assertThat(c.getConnectorId()).isEqualTo("xyz");
    verify(con).getConnectionId();
  }

  @Test
  void isAliveReturnsTrueWhenConnected() throws Exception {
    JMXConnector con = mock(JMXConnector.class);
    when(con.getConnectionId()).thenReturn("abc");
    ConnectionImpl c = new ConnectionImpl(con, SyntaxUtils.getUrl("localhost:9991", null));
    assertThat(c.isAlive()).isTrue();
  }

  @Test
  void isAliveReturnsFalseWhenBroken() throws Exception {
    JMXConnector con = mock(JMXConnector.class);
    when(con.getConnectionId()).thenThrow(new IOException("Connection reset"));
    ConnectionImpl c = new ConnectionImpl(con, SyntaxUtils.getUrl("localhost:9991", null));
    assertThat(c.isAlive()).isFalse();
  }
}
