package org.cyclopsgroup.jmxterm.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** End-to-end integration tests for jmxterm process exit codes. */
class ExitCodeE2EIT {

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
  void testSuccessfulExecution() throws Exception {
    try (JmxTermProcessHelper jmxterm = new JmxTermProcessHelper()) {
      jmxterm.sendCommandAndClose(
          "open localhost:" + targetJvm.getJmxPort(), "domains", "quit");
      jmxterm.readAllOutput(TIMEOUT);
      assertEquals(0, jmxterm.getExitCode(), "Successful execution should return exit code 0");
    }
  }

  @Test
  void testExitOnFailureReturnsNegativeLineNumber() throws Exception {
    try (JmxTermProcessHelper jmxterm = new JmxTermProcessHelper("-e")) {
      // Line 1: valid command (help), Line 2: invalid command (get without connection/bean)
      jmxterm.sendCommandAndClose("help", "get Name");
      jmxterm.readAllOutput(TIMEOUT);
      int exitCode = jmxterm.getExitCode();
      // System.exit(-2) produces 254 on POSIX (unsigned byte: 256 - 2)
      assertTrue(
          exitCode != 0,
          "Expected non-zero exit code for failure with -e, but got: " + exitCode);
      assertTrue(
          exitCode == -2 || exitCode == 254,
          "Exit code should be -2 (or 254 unsigned), but got: " + exitCode);
    }
  }

  @Test
  void testQuitExitCode() throws Exception {
    try (JmxTermProcessHelper jmxterm = new JmxTermProcessHelper()) {
      jmxterm.sendCommandAndClose("quit");
      jmxterm.readAllOutput(TIMEOUT);
      assertEquals(0, jmxterm.getExitCode(), "Quit command should return exit code 0");
    }
  }
}
