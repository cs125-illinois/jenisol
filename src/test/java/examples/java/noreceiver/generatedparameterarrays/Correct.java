package examples.java.noreceiver.generatedparameterarrays;

import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Random;

public class Correct {
  @RandomParameters
  private static One<int[]> valueRandom(int complexity, Random random) {
    return new One<>(new int[] {0, 1, 2});
  }

  public static int value(int[] values) {
    return values.length;
  }
}
