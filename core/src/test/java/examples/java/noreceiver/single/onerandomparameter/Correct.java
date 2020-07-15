package examples.java.noreceiver.single.onerandomparameter;

import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Random;

public class Correct {
  @RandomParameters
  public static One<Long> valueParameters(int complexity, Random random) {
    if (complexity == 1) {
      return new One<>(8888L);
    } else {
      return new One<>(random.nextLong());
    }
  }

  public static boolean value(long first) {
    return first == 8888;
  }
}
