package org.cyclopsgroup.jmxterm.cc;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Test case of {@link PredefinedCommandFactory}
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
class PredefinedCommandFactoryTest {
  /**
   * Test that object is constructed
   *
   * @throws IOException
   */
  @Test
  void construction() throws Exception {
    PredefinedCommandFactory f = new PredefinedCommandFactory();
    assertThat(f.getCommandTypes().containsKey("help")).isTrue();
    assertThat(f.getCommandTypes().containsKey("open")).isTrue();
    assertThat(f.getCommandTypes().containsKey("close")).isTrue();
    assertThat(f.getCommandTypes().containsKey("quit")).isTrue();
    assertThat(f.getCommandTypes().containsKey("beans")).isTrue();
    assertThat(f.createCommand("help")).isInstanceOf(HelpCommand.class);
  }
}
