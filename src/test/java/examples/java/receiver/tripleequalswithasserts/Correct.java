package examples.java.receiver.tripleequalswithasserts;

import edu.illinois.cs.cs125.jenisol.core.EdgeType;
import edu.illinois.cs.cs125.jenisol.core.RandomType;
import edu.illinois.cs.cs125.jenisol.core.SimpleType;
import java.util.Random;

public class Correct {
  private final String string;
  private final double first;
  private final double second;

  public Correct(String setString, double setFirst, double setSecond) {
    assert setString != null;
    assert setFirst >= -90.0 && setFirst <= 90.0;
    assert setSecond >= -180.0 && setSecond <= 180.0;
    string = setString;
    first = setFirst;
    second = setSecond;
  }

  @SimpleType private static final double[] SIMPLE_DOUBLES = new double[] {8.8};

  @EdgeType
  private static final double[] EDGE_DOUBLES =
      new double[] {
        -180.1, -180.0, -179.9, -90.1, -90.0, -89.9, 89.9, 90.0, 90.1, 179.9, 180.0, 180.1
      };

  @RandomType
  private static double randomPosition(Random random) {
    return (random.nextDouble() * 179.8) - 89.9;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Correct correct)) {
      return false;
    }
    return first == correct.first && second == correct.second;
  }
}
