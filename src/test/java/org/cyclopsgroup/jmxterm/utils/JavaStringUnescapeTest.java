package org.cyclopsgroup.jmxterm.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class JavaStringUnescapeTest {

  @Test
  void returnsNullForNullInput() {
    assertNull(JavaStringUnescape.unescapeJava(null));
  }

  @Test
  void returnsEmptyForEmptyInput() {
    assertEquals("", JavaStringUnescape.unescapeJava(""));
  }

  @Test
  void passesPlainTextThrough() {
    assertEquals("hello world", JavaStringUnescape.unescapeJava("hello world"));
  }

  @Test
  void unescapesNewline() {
    assertEquals("a\nb", JavaStringUnescape.unescapeJava("a\\nb"));
  }

  @Test
  void unescapesTab() {
    assertEquals("a\tb", JavaStringUnescape.unescapeJava("a\\tb"));
  }

  @Test
  void unescapesCarriageReturn() {
    assertEquals("a\rb", JavaStringUnescape.unescapeJava("a\\rb"));
  }

  @Test
  void unescapesBackspace() {
    assertEquals("a\bb", JavaStringUnescape.unescapeJava("a\\bb"));
  }

  @Test
  void unescapesFormFeed() {
    assertEquals("a\fb", JavaStringUnescape.unescapeJava("a\\fb"));
  }

  @Test
  void unescapesBackslash() {
    assertEquals("a\\b", JavaStringUnescape.unescapeJava("a\\\\b"));
  }

  @Test
  void unescapesDoubleQuote() {
    assertEquals("a\"b", JavaStringUnescape.unescapeJava("a\\\"b"));
  }

  @Test
  void unescapesSingleQuote() {
    assertEquals("a'b", JavaStringUnescape.unescapeJava("a\\'b"));
  }

  @Test
  void unescapesUnicode() {
    assertEquals("A", JavaStringUnescape.unescapeJava("\\u0041"));
    assertEquals("\u00E9", JavaStringUnescape.unescapeJava("\\u00e9"));
  }

  @Test
  void unescapesOctal() {
    assertEquals("A", JavaStringUnescape.unescapeJava("\\101")); // 'A' = 65 = 0101
    assertEquals("\0", JavaStringUnescape.unescapeJava("\\0"));
  }

  @Test
  void handlesMultipleEscapes() {
    assertEquals("line1\nline2\ttab", JavaStringUnescape.unescapeJava("line1\\nline2\\ttab"));
  }

  @Test
  void handlesTrailingBackslash() {
    assertEquals("end\\", JavaStringUnescape.unescapeJava("end\\"));
  }

  @Test
  void handlesUnknownEscapeSequence() {
    // Unknown escapes just preserve the backslash
    assertEquals("\\x", JavaStringUnescape.unescapeJava("\\x"));
  }
}
