package examples.java.noreceiver.single.onegeneratedparameter;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import edu.illinois.cs.cs125.jenisol.core.Two;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Correct {
  @FixedParameters
  public static final List<Two<Generated, Boolean>> FIXED =
      Arrays.asList(new Two<>(new Generated(8888), false), new Two<>(new Generated(8888), true));

  @RandomParameters
  public static Two<Generated, Boolean> valueRandom(int complexity, Random random) {
    return new Two<>(new Generated(random.nextInt()), random.nextBoolean());
  }

  public static boolean value(Generated first, boolean second) {
    return first.getValue() == 8888 && second;
  }
}
