package examples.java.noreceiver.skiptest;

import edu.illinois.cs.cs125.jenisol.core.SkipTest;

public class Correct {
  public static int value(int first) {
    if (first % 2 == 0) {
      throw new SkipTest();
    }
    return first + 1;
  }
}
