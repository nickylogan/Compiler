package utils;

public class StringUTILS {
  /**
   * This helper method converts an integer to its hexadecimal representation.
   * Zeroes are padded in the beginning of the string until the given length is fulfilled.
   *
   * @param value
   * @param length
   * @return a hex representation of value with the given length
   */
  public static String toHexStringWithLength(int value, int length) {
    StringBuilder sb = new StringBuilder();
    String hex = Integer.toHexString(value & 0xffff);
    for (int i = 0; i < length - hex.length(); ++i) sb.append("0");
    sb.append(hex);
    sb.delete(0, sb.length() - length);
    return sb.toString();
  }
}
