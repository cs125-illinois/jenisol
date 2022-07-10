package examples.java.noreceiver.countarrayequals2d;

import edu.illinois.cs.cs125.jenisol.core.RandomType;
import java.util.Random;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class Correct {
  public int value(int[][] values, int check) {
    int count = 0;
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values[i].length; j++) {
        if (values[i][j] == check) {
          count++;
        }
      }
    }
    return count;
  }

  @RandomType
  private static int randomInt(int complexity, Random random) {
    return random.nextInt(8);
  }
}
