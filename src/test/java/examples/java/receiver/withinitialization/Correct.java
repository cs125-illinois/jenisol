package examples.java.receiver.withinitialization;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;

import java.util.Arrays;
import java.util.List;

public class Correct extends Parent {
  public int getValue() {
    return value;
  }

  @FixedParameters
  private static final List<Integer> FIXED = Arrays.asList(-1, 0, 1);
}
