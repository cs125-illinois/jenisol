package examples.java.noreceiver.stringequality;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.One;
import java.util.Collections;
import java.util.List;

public class Correct {
  public static boolean isEmpty(String input) {
    return input.equals("");
  }

  @FixedParameters
  private static final List<One<String>> SIMPLE = Collections.singletonList(new One<>(""));
}
