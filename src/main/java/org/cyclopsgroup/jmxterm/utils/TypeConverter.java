package org.cyclopsgroup.jmxterm.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Utility to convert string values to target types, replacing commons-beanutils ConvertUtils.
 */
public final class TypeConverter {

  private static final Map<String, Class<?>> PRIMITIVE_TYPES = Map.ofEntries(
      Map.entry("boolean", boolean.class),
      Map.entry("byte", byte.class),
      Map.entry("char", char.class),
      Map.entry("short", short.class),
      Map.entry("int", int.class),
      Map.entry("long", long.class),
      Map.entry("float", float.class),
      Map.entry("double", double.class));

  private TypeConverter() {}

  /**
   * Resolve a class by name, supporting primitive type names.
   *
   * @param type Fully qualified class name or primitive type name
   * @return The resolved class
   * @throws ClassNotFoundException if the class is not found
   */
  public static Class<?> resolveClass(String type) throws ClassNotFoundException {
    Class<?> primitive = PRIMITIVE_TYPES.get(type);
    if (primitive != null) {
      return primitive;
    }
    return Class.forName(type);
  }

  /**
   * Convert a string expression to the target type.
   *
   * @param expression String to convert
   * @param targetType Target class
   * @return Converted value
   */
  public static Object convert(String expression, Class<?> targetType) {
    if (targetType == String.class) {
      return expression;
    }
    if (targetType == boolean.class || targetType == Boolean.class) {
      return Boolean.parseBoolean(expression);
    }
    if (targetType == byte.class || targetType == Byte.class) {
      return Byte.parseByte(expression);
    }
    if (targetType == char.class || targetType == Character.class) {
      if (expression.length() != 1) {
        throw new IllegalArgumentException("Cannot convert \"" + expression + "\" to char");
      }
      return expression.charAt(0);
    }
    if (targetType == short.class || targetType == Short.class) {
      return Short.parseShort(expression);
    }
    if (targetType == int.class || targetType == Integer.class) {
      return Integer.parseInt(expression);
    }
    if (targetType == long.class || targetType == Long.class) {
      return Long.parseLong(expression);
    }
    if (targetType == float.class || targetType == Float.class) {
      return Float.parseFloat(expression);
    }
    if (targetType == double.class || targetType == Double.class) {
      return Double.parseDouble(expression);
    }
    if (targetType == BigInteger.class) {
      return new BigInteger(expression);
    }
    if (targetType == BigDecimal.class) {
      return new BigDecimal(expression);
    }
    throw new IllegalArgumentException(
        "Cannot convert \"" + expression + "\" to type " + targetType.getName());
  }
}
