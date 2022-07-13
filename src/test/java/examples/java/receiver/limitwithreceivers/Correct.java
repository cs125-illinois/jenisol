package examples.java.receiver.limitwithreceivers;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.Limit;
import java.util.Arrays;
import java.util.List;

public class Correct {
  private final int value;

  public Correct(int setValue) {
    value = setValue;
  }

  @Limit(1)
  public int getValue() {
    return value;
  }

  @FixedParameters private static final List<Integer> FIXED = Arrays.asList(-1, 0, 1);
}
