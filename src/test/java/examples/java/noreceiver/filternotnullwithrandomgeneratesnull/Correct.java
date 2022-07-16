package examples.java.noreceiver.filternotnullwithrandomgeneratesnull;

import edu.illinois.cs.cs125.jenisol.core.NotNull;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;

import java.util.Random;

public class Correct {
  public static int getHashCode(@NotNull Object other) {
    return other.hashCode();
  }

  @RandomParameters
  private static Object randomParameters(Random random) {
    if (random.nextBoolean()) {
      return null;
    } else {
      return "test";
    }
  }
}
