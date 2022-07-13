package examples.java.receiver.allmethodslimited;

import edu.illinois.cs.cs125.jenisol.core.Limit;

public class Correct {
  private final int value;

  public Correct(int setValue) {
    value = setValue;
  }

  @Limit(1)
  public int getValue() {
    return value;
  }

  @Limit(1)
  public int getAnotherValue() {
    return value;
  }
}
