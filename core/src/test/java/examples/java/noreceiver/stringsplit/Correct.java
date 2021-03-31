package examples.java.noreceiver.stringsplit;

import edu.illinois.cs.cs125.jenisol.core.FilterParameters;
import edu.illinois.cs.cs125.jenisol.core.SkipTest;

public class Correct {
  public static String part(String string) {
    return string.split(" ")[1];
  }

  @FilterParameters
  public static void filter(String string) {
    if (string != null && !string.trim().equals(string)) {
      throw new SkipTest();
    }
  }
}
