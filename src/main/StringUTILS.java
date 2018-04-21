package main;

public class StringUTILS {
  public static String toHexStringWithLength(int value, int length) {
    StringBuilder sb = new StringBuilder();
    String hex = Integer.toHexString(value & 0xffff);
    for (int i = 0; i < length - hex.length(); ++i) sb.append("0");
    sb.append(hex);
    sb.delete(0, sb.length() - length);
    return sb.toString();
  }
}
