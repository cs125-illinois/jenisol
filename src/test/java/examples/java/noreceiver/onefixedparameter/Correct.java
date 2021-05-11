package examples.java.noreceiver.onefixedparameter;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.One;
import java.util.Collections;
import java.util.List;

public class Correct {
  @FixedParameters
  public static final List<One<Long>> SIMPLE = Collections.singletonList(new One<>(8888L));

  public static boolean value(long first) {
    return first == 8888;
  }
}
