package examples.java.receiver.withinitialization;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.Limit;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Correct extends Parent {
  @Limit(1)
  public int getValue() {
    return value;
  }

  @FixedParameters private static final List<Integer> FIXED = Arrays.asList(-1, 0, 1);

  @RandomParameters
  private static int randomParameters(int complexity, Random random) {
    return random.nextInt(complexity);
  }
}
