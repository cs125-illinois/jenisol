package examples.java.noreceiver.countarrayequals1d;

import edu.illinois.cs.cs125.jenisol.core.RandomType;
import java.util.Random;

public class Correct {
  public int value(int[] values, int check) {
    int count = 0;
    for (int i = 0; i < values.length; i++) {
      if (values[i] == check) {
        count++;
      }
    }
    return count;
  }

  @RandomType
  private static int randomInt(int complexity, Random random) {
    return random.nextInt(16);
  }
}
