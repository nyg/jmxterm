package org.cyclopsgroup.jmxterm.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** End-to-end integration tests for jmxterm CLI argument handling. */
class CliArgumentsE2EIT {

  private static final Duration TIMEOUT = Duration.ofSeconds(30);
  private static TargetJvmProcess targetJvm;

  @BeforeAll
  static void startTargetJvm() throws Exception {
    targetJvm = new TargetJvmProcess();
    targetJvm.waitUntilReady(TIMEOUT);
  }

  @AfterAll
  static void stopTargetJvm() {
    if (targetJvm != null) {
      targetJvm.close();
    }
  }

  @Test
  void testAutoConnect() throws Exception {
    try (JmxTermProcessHelper jmxterm =
        new JmxTermProcessHelper("-l", "localhost:" + targetJvm.getJmxPort())) {
      jmxterm.sendCommandAndClose("domains", "quit");
      String output = jmxterm.readAllOutput(TIMEOUT);
      assertTrue(
          output.contains("JMImplementation"),
          "Expected 'JMImplementation' domain in output: " + output);
    }
  }

  @Test
  void testSilentMode() throws Exception {
    try (JmxTermProcessHelper jmxterm = new JmxTermProcessHelper("-v", "silent")) {
      jmxterm.sendCommandAndClose(
          "open localhost:" + targetJvm.getJmxPort(),
          "bean test:type=TestMBean",
          "get Name",
          "quit");
      String output = jmxterm.readAllOutput(TIMEOUT);
      // In silent mode, informational messages prefixed with "#" should not appear
      for (String line : output.split("\\R")) {
        assertFalse(
            line.startsWith("#"),
            "Silent mode should not produce '#' prefixed lines, but found: " + line);
      }
      // The attribute value should still be present
      assertTrue(output.contains("default"), "Expected 'default' value in output: " + output);
    }
  }

  @Test
  void testExitOnFailure() throws Exception {
    try (JmxTermProcessHelper jmxterm = new JmxTermProcessHelper("-e")) {
      // Send a command that fails (getting an attribute without a connection)
      jmxterm.sendCommandAndClose("get Name");
      String output = jmxterm.readAllOutput(TIMEOUT);
      int exitCode = jmxterm.getExitCode();
      assertNotEquals(0, exitCode, "Expected non-zero exit code for failed command, output: " + output);
    }
  }

  @Test
  void testHelpFlag() throws Exception {
    try (JmxTermProcessHelper jmxterm = new JmxTermProcessHelper("-h")) {
      String output = jmxterm.readAllOutput(TIMEOUT);
      int exitCode = jmxterm.getExitCode();
      assertTrue(
          output.contains("usage") || output.contains("Usage") || output.contains("jmxterm"),
          "Expected usage information in output: " + output);
      assertEquals(0, exitCode, "Help flag should result in exit code 0");
    }
  }
}
