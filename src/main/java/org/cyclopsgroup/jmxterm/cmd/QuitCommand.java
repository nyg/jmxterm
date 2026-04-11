package org.cyclopsgroup.jmxterm.cmd;

import java.io.IOException;
import org.cyclopsgroup.jmxterm.Command;
import org.cyclopsgroup.jmxterm.Session;

/**
 * Command to terminate the console
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
@picocli.CommandLine.Command(name = "quit", description = "Terminate console and exit")
public class QuitCommand extends Command {
  @Override
  public void execute() throws IOException {
    Session session = getSession();
    session.disconnect();
    session.close();
    session.getOutput().printMessage("bye");
  }
}
