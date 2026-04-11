package org.cyclopsgroup.jmxterm.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.management.JMException;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import org.cyclopsgroup.jmxterm.Command;
import org.cyclopsgroup.jmxterm.Session;
import org.cyclopsgroup.jmxterm.io.ValueOutputFormat;

/**
 * Command to show about page
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
@CommandLine.Command(name = "about", description = "Display about page")
public class AboutCommand extends Command {
  private boolean showDescription;

  @Override
  public void execute() throws IOException, JMException {
    Session session = getSession();
    ValueOutputFormat format = new ValueOutputFormat(2, showDescription, true);

    // output predefined about properties
    Properties props = new Properties();
    try (InputStream in =
        getClass().getClassLoader().getResourceAsStream("META-INF/jmxsh/about.properties")) {
      if (in != null) {
        props.load(in);
      }
    }
    for (String key : new TreeMap<>(props).keySet().stream().map(Object::toString).toList()) {
      format.printExpression(session.getOutput(), key, props.getProperty(key), null);
    }

    // output Java runtime properties
    for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
      String keyName = entry.toString();
      if (keyName.startsWith("java.")) {
        format.printExpression(session.getOutput(), keyName, entry.getValue(), null);
      }
    }
  }

  /** @param showDescription True to show detail description */
  @Option(names = {"-s", "--show"}, description = "Show detail description")
  public final void setShowDescription(boolean showDescription) {
    this.showDescription = showDescription;
  }
}
