package org.cyclopsgroup.jmxterm.cc;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.cyclopsgroup.jmxterm.MockSession;
import org.cyclopsgroup.jmxterm.SelfRecordingCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link HelpCommand}
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
class HelpCommandTest {
  private HelpCommand command;

  private StringWriter output;

  /** Set up objects to test */
  @BeforeEach
  void setUp() {
    command = new HelpCommand();
    output = new StringWriter();
  }

  /**
   * Test execution with several options
   *
   * @throws IOException
   * @throws IntrospectionException
   */
  @Test
  void executeWithOption() throws Exception {
    command.setArgNames(Arrays.asList("a", "b"));
    CommandCenter cc = mock(CommandCenter.class);
    command.setCommandCenter(cc);

    doReturn(SelfRecordingCommand.class).when(cc).getCommandType("a");
    doReturn(SelfRecordingCommand.class).when(cc).getCommandType("b");
    doReturn(new SelfRecordingCommand(new ArrayList<>())).when(cc).createCommand("a");
    doReturn(new SelfRecordingCommand(new ArrayList<>())).when(cc).createCommand("b");
    command.setSession(new MockSession(output, null));
    command.execute();
    verify(cc).getCommandType("a");
    verify(cc).getCommandType("b");
  }

  /**
   * Test execution without option
   *
   * @throws IOException
   */
  @Test
  void executeWithoutOption() throws Exception {
    CommandCenter cc = mock(CommandCenter.class);
    command.setCommandCenter(cc);
    when(cc.getCommandNames()).thenReturn(new HashSet<String>(Arrays.asList("a", "b")));
    doReturn(SelfRecordingCommand.class).when(cc).getCommandType("a");
    doReturn(SelfRecordingCommand.class).when(cc).getCommandType("b");
    doReturn(new SelfRecordingCommand(new ArrayList<>())).when(cc).createCommand("a");
    doReturn(new SelfRecordingCommand(new ArrayList<>())).when(cc).createCommand("b");
    command.setSession(new MockSession(output, null));
    command.execute();
    assertThat(output.toString().trim())
        .isEqualTo("a        - desc" + System.lineSeparator() + "b        - desc");
  }
}
