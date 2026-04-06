package org.cyclopsgroup.jmxterm.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import org.cyclopsgroup.jmxterm.cc.CommandCenter;
import org.cyclopsgroup.jmxterm.io.WriterCommandOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Integration tests for browsing domains, beans, and MBean info via a real JMX connection. */
class DomainBeanNavigationIT {

  @RegisterExtension static EmbeddedJmxServer jmxServer = new EmbeddedJmxServer();

  private CommandCenter cc;
  private StringWriter resultWriter;
  private StringWriter messageWriter;

  @BeforeEach
  void setUp() throws Exception {
    resultWriter = new StringWriter();
    messageWriter = new StringWriter();
    cc = new CommandCenter(new WriterCommandOutput(resultWriter, messageWriter), null);
    assertTrue(cc.execute("open " + jmxServer.getConnectionUrl()));
  }

  @AfterEach
  void tearDown() {
    cc.close();
  }

  @Test
  void testListDomains() {
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("domains"));
    String result = resultWriter.toString();
    assertTrue(result.contains("test"), "Expected 'test' domain, got: " + result);
    assertTrue(
        result.contains("JMImplementation"),
        "Expected 'JMImplementation' domain, got: " + result);
  }

  @Test
  void testSelectDomain() {
    assertTrue(cc.execute("domain test"));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("domain"));
    String result = resultWriter.toString();
    assertTrue(result.contains("test"), "Expected current domain 'test', got: " + result);
  }

  @Test
  void testListBeans() {
    assertTrue(cc.execute("domain test"));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("beans"));
    String result = resultWriter.toString();
    assertTrue(
        result.contains("test:type=TestMBean"),
        "Expected 'test:type=TestMBean' in beans output, got: " + result);
  }

  @Test
  void testSelectBean() {
    assertTrue(cc.execute("bean test:type=TestMBean"));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("bean"));
    String result = resultWriter.toString();
    assertTrue(
        result.contains("test:type=TestMBean"),
        "Expected current bean 'test:type=TestMBean', got: " + result);
  }

  @Test
  void testListBeansWithDomainOption() {
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("beans -d test"));
    String result = resultWriter.toString();
    assertTrue(
        result.contains("test:type=TestMBean"),
        "Expected 'test:type=TestMBean' in beans output, got: " + result);
  }

  @Test
  void testInfoCommand() {
    assertTrue(cc.execute("bean test:type=TestMBean"));
    resultWriter.getBuffer().setLength(0);
    assertTrue(cc.execute("info"));
    String result = resultWriter.toString();
    // Verify attributes are listed
    assertTrue(result.contains("Name"), "Expected attribute 'Name' in info, got: " + result);
    assertTrue(result.contains("Count"), "Expected attribute 'Count' in info, got: " + result);
    // Verify operations are listed
    assertTrue(result.contains("echo"), "Expected operation 'echo' in info, got: " + result);
    assertTrue(result.contains("add"), "Expected operation 'add' in info, got: " + result);
    assertTrue(result.contains("reset"), "Expected operation 'reset' in info, got: " + result);
  }
}
