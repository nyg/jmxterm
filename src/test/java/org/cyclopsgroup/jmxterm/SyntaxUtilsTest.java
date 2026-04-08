package org.cyclopsgroup.jmxterm;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Test case of {@link SyntaxUtils}
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
class SyntaxUtilsTest {
  /**
   * Test how getUrl() figure out MBeanServer URL based on various pattern of input
   *
   * @throws IOException Thrown when syntax is invalid
   */
  @Test
  void getUrl() throws Exception {
    assertThat(SyntaxUtils.getUrl("xyz-host.cyclopsgroup.org:12345", null).getURLPath())
        .isEqualTo("/jndi/rmi://xyz-host.cyclopsgroup.org:12345/jmxrmi");
    assertThat(
        SyntaxUtils.getUrl(
                "service:jmx:rmi:///jndi/rmi://xyz-host.cyclopsgroup.org:12345/jmxrmi", null)
            .getURLPath())
        .isEqualTo("/jndi/rmi://xyz-host.cyclopsgroup.org:12345/jmxrmi");
  }

  /** Verify string expression of type is correctly parsed */
  @Test
  void parseNormally() {
    assertThat(SyntaxUtils.parse("x", "java.lang.String")).isEqualTo("x");
    assertThat(SyntaxUtils.parse("3", "int")).isEqualTo(3);
    assertThat(SyntaxUtils.parse("3", "long")).isEqualTo(3L);
    assertThat(SyntaxUtils.parse("", "java.lang.String")).isEqualTo("");
    assertThat(SyntaxUtils.parse("", "java.util.Date")).isNull();
    assertThat(SyntaxUtils.parse("null", "java.lang.String")).isNull();
  }

  /** Verify that Exception is thrown when type is wrong */
  @Test
  void parseWithWrongType() {
    assertThatThrownBy(() -> SyntaxUtils.parse("x", "x"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
