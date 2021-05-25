package examples.java.noreceiver.randomparameternocomplexity;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Correct {
  @FixedParameters private static final List<One<Long>> FIXED = Arrays.asList(new One<>(8888L));

  @RandomParameters
  private static One<Long> valueParameters(Random random) {
    return new One<>(random.nextLong());
  }

  public static boolean value(long first) {
    return first == 8888;
  }
}
