package examples.java.noreceiver.filternotnullwithrandomgeneratesnull;

import edu.illinois.cs.cs125.jenisol.core.NotNull;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;

import java.util.Random;

public class Correct0 {
  public static int getHashCode(@NotNull Object other) {
    if (other == null) {
      return 0;
    }
    return other.hashCode();
  }
}
