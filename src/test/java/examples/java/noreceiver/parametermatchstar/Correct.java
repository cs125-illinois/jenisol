package examples.java.noreceiver.parametermatchstar;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Correct {
  public static int first(Special special) {
    return special.getFirst();
  }

  public static String second(Special special) {
    return special.getSecond();
  }

  @FixedParameters("*")
  private static final List<Special> FIXED =
      Arrays.asList(new Special(0, ""), new Special(1, "one"));

  @RandomParameters("*")
  private static Special randomSpecial(Random random) {
    return new Special(random.nextInt(), "x".repeat(random.nextInt(32)));
  }
}
