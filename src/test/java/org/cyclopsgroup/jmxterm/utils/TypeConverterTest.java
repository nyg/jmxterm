package org.cyclopsgroup.jmxterm.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

class TypeConverterTest {

  @Test
  void resolvesPrimitiveTypes() throws ClassNotFoundException {
    assertEquals(int.class, TypeConverter.resolveClass("int"));
    assertEquals(long.class, TypeConverter.resolveClass("long"));
    assertEquals(boolean.class, TypeConverter.resolveClass("boolean"));
    assertEquals(double.class, TypeConverter.resolveClass("double"));
    assertEquals(float.class, TypeConverter.resolveClass("float"));
    assertEquals(short.class, TypeConverter.resolveClass("short"));
    assertEquals(byte.class, TypeConverter.resolveClass("byte"));
    assertEquals(char.class, TypeConverter.resolveClass("char"));
  }

  @Test
  void resolvesWrapperTypes() throws ClassNotFoundException {
    assertEquals(Integer.class, TypeConverter.resolveClass("java.lang.Integer"));
    assertEquals(String.class, TypeConverter.resolveClass("java.lang.String"));
  }

  @Test
  void resolveClassThrowsForUnknown() {
    assertThrows(ClassNotFoundException.class, () -> TypeConverter.resolveClass("no.such.Class"));
  }

  @Test
  void convertsString() {
    assertEquals("hello", TypeConverter.convert("hello", String.class));
  }

  @Test
  void convertsBoolean() {
    assertEquals(true, TypeConverter.convert("true", boolean.class));
    assertEquals(false, TypeConverter.convert("false", Boolean.class));
  }

  @Test
  void convertsInteger() {
    assertEquals(42, TypeConverter.convert("42", int.class));
    assertEquals(42, TypeConverter.convert("42", Integer.class));
  }

  @Test
  void convertsLong() {
    assertEquals(123456789L, TypeConverter.convert("123456789", long.class));
    assertEquals(123456789L, TypeConverter.convert("123456789", Long.class));
  }

  @Test
  void convertsDouble() {
    assertEquals(3.14, TypeConverter.convert("3.14", double.class));
    assertEquals(3.14, TypeConverter.convert("3.14", Double.class));
  }

  @Test
  void convertsFloat() {
    assertEquals(1.5f, TypeConverter.convert("1.5", float.class));
    assertEquals(1.5f, TypeConverter.convert("1.5", Float.class));
  }

  @Test
  void convertsShort() {
    assertEquals((short) 7, TypeConverter.convert("7", short.class));
    assertEquals((short) 7, TypeConverter.convert("7", Short.class));
  }

  @Test
  void convertsByte() {
    assertEquals((byte) 3, TypeConverter.convert("3", byte.class));
    assertEquals((byte) 3, TypeConverter.convert("3", Byte.class));
  }

  @Test
  void convertsChar() {
    assertEquals('A', TypeConverter.convert("A", char.class));
    assertEquals('A', TypeConverter.convert("A", Character.class));
  }

  @Test
  void charConversionRejectsMultipleChars() {
    assertThrows(IllegalArgumentException.class, () -> TypeConverter.convert("AB", char.class));
  }

  @Test
  void convertsBigInteger() {
    assertEquals(new BigInteger("999999999999"), TypeConverter.convert("999999999999", BigInteger.class));
  }

  @Test
  void convertsBigDecimal() {
    assertEquals(new BigDecimal("1.23456789"), TypeConverter.convert("1.23456789", BigDecimal.class));
  }

  @Test
  void throwsForUnsupportedType() {
    assertThrows(IllegalArgumentException.class, () -> TypeConverter.convert("x", Object.class));
  }
}
