package examples.java.noreceiver.single.randomtype;

import edu.illinois.cs.cs125.jenisol.core.RandomType;
import java.util.Random;

public class Correct {
  @RandomType
  public static int random(int complexity, Random random) {
    if (complexity == 1) {
      return 8888;
    } else if (complexity == 2) {
      return 888888;
    } else {
      return random.nextInt();
    }
  }

  public static boolean value(int first) {
    return first == 8888 || first == 888888;
  }
}
