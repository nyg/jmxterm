package org.cyclopsgroup.jmxterm.cmd;

import java.io.IOException;
import org.cyclopsgroup.jmxterm.Command;

/**
 * Command to close current connection
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
@picocli.CommandLine.Command(name = "close", description = "Close current JMX connection")
public class CloseCommand extends Command {
  @Override
  public void execute() throws IOException {
    getSession().disconnect();
    getSession().getOutput().printMessage("disconnected");
  }
}
