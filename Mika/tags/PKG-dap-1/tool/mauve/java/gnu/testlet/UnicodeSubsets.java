
package gnu.testlet;

import java.util.ArrayList;

/**
 * Analyse the mika.unicode.subsets system property to see which subsets
 * are supposed to be supported. This can be used to skip tests which are
 * bound to fail because they use unsupported regions of unicode.
 */
public class UnicodeSubsets {

  private static String unicode_subsets_property;
  private static boolean support_all_subsets;
  private static ArrayList unicode_subsets;

  static {
      unicode_subsets_property = System.getProperty("mika.unicode.subsets", "999");
      unicode_subsets = new ArrayList();
      java.util.StringTokenizer toks = new java.util.StringTokenizer(unicode_subsets_property);
      while (toks.hasMoreTokens()) {
        unicode_subsets.add(toks.nextToken());
      }
      if (unicode_subsets.contains("999")) {
        support_all_subsets = true;
      }
  }

  public static boolean isSupported(String s) {
    return support_all_subsets || unicode_subsets.contains(s);
  }
}
