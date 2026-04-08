package org.cyclopsgroup.jmxterm.utils;

/**
 * This is an utility to parse string value from input. It's only to parse a value such as MBean
 * attribute value or parameter of operation. It's NOT designed to parse MBean name or other type of
 * input.
 *
 * @author $Author$
 * @version $Revision$ in $Change$ submitted at $DateTime$
 */
public final class ValueFormat {
  /** Keyword that identifies NULL pointer <code>null</code> */
  public static final String NULL = "null";

  private ValueFormat() {}

  /**
   * Parse given syntax of string
   *
   * @param value String value
   * @return Escaped string value
   */
  public static String parseValue(String value) {
    if (value == null || value.isEmpty()) {
      return null;
    }
    if (NULL.equals(value)) {
      return null;
    }
    String s;
    if (value.charAt(0) == '\"' && value.charAt(value.length() - 1) == '\"') {
      s = value.substring(1, value.length() - 1);
    } else {
      s = value;
    }
    return JavaStringUnescape.unescapeJava(s);
  }
}
