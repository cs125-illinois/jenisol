package examples.java.noreceiver.sourcechecker;

import edu.illinois.cs.cs125.jenisol.core.CheckSource;
import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import edu.illinois.cs.cs125.jenisol.core.SourceCheckError;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Correct {
  public static long factorial(long input) {
    if (input < 0 || input > 20) {
      throw new IllegalArgumentException();
    }
    if (input == 0) {
      return 1;
    }
    return input * factorial(input - 1);
  }

  @FixedParameters
  private static final List<One<Long>> FIXED =
      Arrays.asList(
          new One<>(-1L),
          new One<>(0L),
          new One<>(1L),
          new One<>(10L),
          new One<>(20L),
          new One<>(21L));

  @RandomParameters
  private static One<Long> randomParameters(int complexity, Random random) {
    return new One<>(random.nextLong() % 23 - 1);
  }

  @CheckSource
  private static void checkSource(String source) {
    if (source.contains("for")) {
      throw new SourceCheckError("Submission uses a loop");
    }
  }
}
