package examples.java.noreceiver.randomtypenocomplexity;

import edu.illinois.cs.cs125.jenisol.core.RandomType;
import java.util.Random;

public class Correct {
  @RandomType
  private static int random(Random random) {
    return random.nextInt(64);
  }

  public static boolean value(int first) {
    return first < 32;
  }
}
