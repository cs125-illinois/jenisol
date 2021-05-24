package examples.java.noreceiver.twofixedparameters;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.Two;
import java.util.Arrays;
import java.util.List;

public class Correct {
  @FixedParameters
  private static final List<Two<Integer, Boolean>> SIMPLE =
      Arrays.asList(new Two<>(8888, false), new Two<>(8888, true));

  public static boolean value(int first, boolean second) {
    return first == 8888 && second;
  }
}
