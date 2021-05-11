package examples.java.noreceiver.kotlinrawtypes;

import edu.illinois.cs.cs125.jenisol.core.EdgeType;
import edu.illinois.cs.cs125.jenisol.core.RandomType;
import edu.illinois.cs.cs125.jenisol.core.SimpleType;
import java.util.Random;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Correct {
  public static Object getValue(Tester tester) {
    return tester.getValue();
  }

  @SimpleType private static final Tester[] SIMPLE = new Tester[] {new Tester(1), new Tester("1")};

  @EdgeType private static final Tester[] EDGE = new Tester[] {};

  @RandomType
  private static Tester randomTester(int complexity, Random random) {
    if (random.nextBoolean()) {
      return new Tester(random.nextInt());
    } else {
      return new Tester(random.nextFloat());
    }
  }
}
