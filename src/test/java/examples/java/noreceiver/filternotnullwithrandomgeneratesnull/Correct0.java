package examples.java.noreceiver.filternotnullwithrandomgeneratesnull;

import edu.illinois.cs.cs125.jenisol.core.NotNull;

public class Correct0 {
  public static int getHashCode(@NotNull Object other) {
    if (other == null) {
      return 0;
    }
    return other.hashCode();
  }
}
