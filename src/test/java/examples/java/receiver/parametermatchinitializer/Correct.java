package examples.java.receiver.parametermatchinitializer;

import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Random;

public class Correct extends Parent {
  public int getValue() {
    return getValue();
  }

  @RandomParameters
  private static int randomParameters(Random random) {
    return random.nextInt();
  }
}
