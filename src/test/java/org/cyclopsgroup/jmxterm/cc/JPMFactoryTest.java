package org.cyclopsgroup.jmxterm.cc;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test of {@link JPMFactory}
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
class JPMFactoryTest {
  /** Verify JPMFactory can create process manager */
  @Test
  void load() {
    assertThat(JPMFactory.createProcessManager()).isNotNull();
  }
}
