package examples.java.noreceiver.filtertest;

import edu.illinois.cs.cs125.jenisol.core.FilterParameters;
import edu.illinois.cs.cs125.jenisol.core.SkipTest;

public class Correct {
  @FilterParameters
  public static void filterValue(int first) {
    if (first % 2 == 0) {
      throw new SkipTest();
    }
  }

  public static int value(int first) {
    return first + 1;
  }
}
