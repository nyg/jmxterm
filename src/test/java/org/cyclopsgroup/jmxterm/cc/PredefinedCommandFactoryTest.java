package org.cyclopsgroup.jmxterm.cc;

import static org.assertj.core.api.Assertions.*;

import org.cyclopsgroup.jmxterm.cmd.QuitCommand;
import org.junit.jupiter.api.Test;

/** Test case of {@link PredefinedCommandFactory} */
class PredefinedCommandFactoryTest {

  @Test
  void construction() {
    PredefinedCommandFactory f = new PredefinedCommandFactory();
    assertThat(f.getCommandTypes()).containsKey("help");
    assertThat(f.getCommandTypes()).containsKey("open");
    assertThat(f.getCommandTypes()).containsKey("close");
    assertThat(f.getCommandTypes()).containsKey("quit");
    assertThat(f.getCommandTypes()).containsKey("beans");
    assertThat(f.createCommand("help")).isInstanceOf(HelpCommand.class);
  }

  @Test
  void aliasesResolveToSameCommand() {
    PredefinedCommandFactory f = new PredefinedCommandFactory();
    assertThat(f.getCommandTypes()).containsKey("exit");
    assertThat(f.getCommandTypes()).containsKey("bye");
    assertThat(f.getCommandTypes().get("exit")).isEqualTo(QuitCommand.class);
    assertThat(f.getCommandTypes().get("bye")).isEqualTo(QuitCommand.class);
    assertThat(f.getCommandTypes().get("quit")).isEqualTo(QuitCommand.class);
  }
}
