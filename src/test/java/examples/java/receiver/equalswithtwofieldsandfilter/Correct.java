package examples.java.receiver.equalswithtwofieldsandfilter;

import edu.illinois.cs.cs125.jenisol.core.FilterParameters;
import edu.illinois.cs.cs125.jenisol.core.SkipTest;

public class Correct {
  private final double first;
  private final double second;

  public Correct(double setFirst, double setSecond) {
    first = setFirst;
    second = setSecond;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Correct)) {
      return false;
    }
    Correct correct = (Correct) o;
    return first == correct.first && second == correct.second;
  }

  @FilterParameters
  private static void filterConstructor(double setFirst, double setSecond) {
    if (setFirst <= 0 && setSecond <= 0) {
      throw new SkipTest();
    }
  }
}
