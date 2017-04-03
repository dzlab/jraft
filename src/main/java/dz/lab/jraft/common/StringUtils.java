package dz.lab.jraft.common;

/**
 * A collection of utilities for dealing with strings.
 */
public class StringUtils {

  /**
   * Check if the given string is empty or null.
   * @param str
   * @return boolean
   */
  public static boolean isEmpty(String str) {
        return str==null || str.equals("");
    }

  /**
   * Check whether two strings have the same value or not.
   * @param str1
   * @param str2
   * @return boolean
   */
  public static boolean equals(String str1, String str2) {
        return (str1==null && str2==null) || str1.equals(str2);
    }
}
