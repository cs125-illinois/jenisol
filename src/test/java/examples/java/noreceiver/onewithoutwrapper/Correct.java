package examples.java.noreceiver.onewithoutwrapper;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Correct {
  @FixedParameters private static final List<String> FIXED = Arrays.asList(null, "8888");

  @RandomParameters
  private static String valueParameters(int complexity, Random random) {
    if (random.nextBoolean()) {
      return null;
    } else {
      return "";
    }
  }

  public static boolean value(String first) {
    return first.equals("8888");
  }
}
