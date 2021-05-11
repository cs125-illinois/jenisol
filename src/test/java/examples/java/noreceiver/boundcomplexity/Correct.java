package examples.java.noreceiver.boundcomplexity;

import edu.illinois.cs.cs125.jenisol.core.BoundComplexity;

public class Correct {
  public static int value(int first) {
    if (first > 1024) {
      throw new BoundComplexity();
    }
    return first + 1;
  }
}
