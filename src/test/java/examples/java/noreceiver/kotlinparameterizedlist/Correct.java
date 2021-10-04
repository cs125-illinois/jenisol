package examples.java.noreceiver.kotlinparameterizedlist;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Correct {
  public static int listSize(List<Item> values) {
    return values.size();
  }

  @FixedParameters private static final List<List<Item>> FIXED = Arrays.asList(null, List.of());

  @RandomParameters
  private static List<Item> randomParameters(Random random) {
    return Arrays.asList(new Item[random.nextInt(32)]);
  }
}
