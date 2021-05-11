package examples.java.receiver.filterreceivers;

import edu.illinois.cs.cs125.jenisol.core.FilterParameters;
import edu.illinois.cs.cs125.jenisol.core.SkipTest;

public class Correct0 {
  private final int value;

  public Correct0(int setValue) {
    value = setValue;
  }

  @FilterParameters
  private static void filterConstructor(int setValue) {
    if (setValue < 0) {
      throw new SkipTest();
    }
  }

  public int getValue() {
    return Math.abs(value);
  }
}
