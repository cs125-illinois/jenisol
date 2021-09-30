package examples.java.noreceiver.kotlinobjectlist;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Correct {
  public static int listSize(List<Object> values) {
    return values.size();
  }

  @FixedParameters
  private static final List<List<Object>> FIXED =
      Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList("1"));

  @RandomParameters
  private static List<Object> randomParameters(Random random) {
    return Arrays.asList(1, 2, 4);
  }
}
