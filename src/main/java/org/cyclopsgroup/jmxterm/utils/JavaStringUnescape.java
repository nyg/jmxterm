package org.cyclopsgroup.jmxterm.utils;

/**
 * Utility to unescape Java string literals, replacing commons-text StringEscapeUtils.unescapeJava.
 */
public final class JavaStringUnescape {

  private JavaStringUnescape() {}

  /**
   * Unescape a Java string literal. Handles standard escape sequences: backslash, double quote,
   * single quote, n, r, t, b, f, unicode escapes (backslash u followed by 4 hex digits),
   * and octal escapes (backslash followed by up to 3 octal digits).
   *
   * @param input String with escape sequences
   * @return Unescaped string, or null if input is null
   */
  public static String unescapeJava(String input) {
    if (input == null) {
      return null;
    }
    int length = input.length();
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      char ch = input.charAt(i);
      if (ch != '\\' || i + 1 >= length) {
        sb.append(ch);
        continue;
      }
      char next = input.charAt(i + 1);
      switch (next) {
        case '\\' -> { sb.append('\\'); i++; }
        case '"' -> { sb.append('"'); i++; }
        case '\'' -> { sb.append('\''); i++; }
        case 'n' -> { sb.append('\n'); i++; }
        case 'r' -> { sb.append('\r'); i++; }
        case 't' -> { sb.append('\t'); i++; }
        case 'b' -> { sb.append('\b'); i++; }
        case 'f' -> { sb.append('\f'); i++; }
        case 'u' -> {
          if (i + 5 < length) {
            String hex = input.substring(i + 2, i + 6);
            try {
              sb.append((char) Integer.parseInt(hex, 16));
              i += 5;
            } catch (NumberFormatException e) {
              sb.append(ch);
            }
          } else {
            sb.append(ch);
          }
        }
        default -> {
          if (next >= '0' && next <= '7') {
            // Octal escape: 1-3 digits, max value \377
            int end = i + 2;
            int limit = Math.min(i + 4, length);
            while (end < limit && input.charAt(end) >= '0' && input.charAt(end) <= '7') {
              end++;
            }
            String octal = input.substring(i + 1, end);
            int value = Integer.parseInt(octal, 8);
            if (value > 0377) {
              // Too large, back off one digit
              octal = input.substring(i + 1, end - 1);
              value = Integer.parseInt(octal, 8);
              end--;
            }
            sb.append((char) value);
            i = end - 1;
          } else {
            sb.append(ch);
          }
        }
      }
    }
    return sb.toString();
  }
}
