package org.cyclopsgroup.jmxterm.boot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import picocli.CommandLine.IVersionProvider;

/** Reads the application version from the Maven-filtered about.properties resource. */
public class VersionProvider implements IVersionProvider {

  @Override
  public String[] getVersion() throws Exception {
    Properties props = new Properties();
    try (InputStream in =
        getClass().getClassLoader().getResourceAsStream("META-INF/jmxsh/about.properties")) {
      if (in == null) {
        return new String[] {"jmxsh (unknown version)"};
      }
      props.load(in);
    }
    String version = props.getProperty("release.version", "unknown");
    String name = props.getProperty("software.name", "jmxsh");
    return new String[] {name + " " + version};
  }
}
