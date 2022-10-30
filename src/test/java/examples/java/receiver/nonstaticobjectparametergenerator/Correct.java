package examples.java.receiver.nonstaticobjectparametergenerator;

import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Random;

@SuppressWarnings("StringOperationCanBeSimplified")
public class Correct {

  public boolean isA(Object test) {
    return test instanceof String;
  }

  @RandomParameters
  private Object randomString(Random random) {
    if (random.nextBoolean()) {
      return new String("Test");
    } else {
      return 10;
    }
  }
}
