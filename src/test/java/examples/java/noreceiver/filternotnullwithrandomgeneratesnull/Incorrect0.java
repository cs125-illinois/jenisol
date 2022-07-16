package examples.java.noreceiver.filternotnullwithrandomgeneratesnull;

import edu.illinois.cs.cs125.jenisol.core.NotNull;

public class Incorrect0 {
  public static int getHashCode(@NotNull Object other) {
    return 1;
  }
}
