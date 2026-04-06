package org.cyclopsgroup.jmxterm.e2e;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Launches {@link TestTargetApp} as a subprocess with JMX remote enabled. Provides the JMX port
 * for test connections and manages the lifecycle of the target JVM.
 */
public class TargetJvmProcess implements AutoCloseable {

  private final Process process;
  private final int jmxPort;

  /**
   * Starts the target JVM with JMX remote enabled on a random free port.
   *
   * @throws IOException if the process cannot be started
   */
  public TargetJvmProcess() throws IOException {
    this.jmxPort = findFreePort();
    String javaHome = System.getProperty("java.home");
    String javaBin = javaHome + "/bin/java";
    String classpath = System.getProperty("java.class.path");

    ProcessBuilder pb =
        new ProcessBuilder(
            javaBin,
            "-cp",
            classpath,
            "-Dcom.sun.management.jmxremote",
            "-Dcom.sun.management.jmxremote.port=" + jmxPort,
            "-Dcom.sun.management.jmxremote.authenticate=false",
            "-Dcom.sun.management.jmxremote.ssl=false",
            "-Dcom.sun.management.jmxremote.local.only=true",
            "org.cyclopsgroup.jmxterm.e2e.TestTargetApp");
    pb.redirectErrorStream(true);
    this.process = pb.start();
  }

  /**
   * Waits until the target JVM prints "READY" to stdout.
   *
   * @param timeout maximum time to wait
   * @throws IOException if reading stdout fails
   * @throws IllegalStateException if the timeout expires before "READY" is received
   */
  public void waitUntilReady(Duration timeout) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    CompletableFuture<Boolean> future =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                String line;
                while ((line = reader.readLine()) != null) {
                  if (line.contains("READY")) {
                    return true;
                  }
                }
                return false;
              } catch (IOException e) {
                return false;
              }
            });
    try {
      boolean ready = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
      if (!ready) {
        process.destroyForcibly();
        throw new IllegalStateException("Target JVM exited without printing READY");
      }
    } catch (TimeoutException e) {
      process.destroyForcibly();
      throw new IllegalStateException(
          "Target JVM did not become ready within " + timeout.toSeconds() + " seconds");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
      throw new IllegalStateException("Interrupted while waiting for target JVM", e);
    } catch (ExecutionException e) {
      process.destroyForcibly();
      throw new IllegalStateException("Error while waiting for target JVM", e.getCause());
    }
  }

  /** @return the JMX remote port of the target JVM */
  public int getJmxPort() {
    return jmxPort;
  }

  /** @return the OS process ID of the target JVM */
  public long getPid() {
    return process.pid();
  }

  @Override
  public void close() {
    process.destroyForcibly();
    try {
      process.waitFor(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private static int findFreePort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }
}
