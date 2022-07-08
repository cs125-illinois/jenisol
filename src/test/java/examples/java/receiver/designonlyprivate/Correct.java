package examples.java.receiver.designonlyprivate;

import edu.illinois.cs.cs125.jenisol.core.CheckDesign;

public class Correct {
  private final int value;

  public Correct(int setValue) {
    value = setValue;
  }

  @CheckDesign
  private int helper(int value) {
    return value + 1;
  }

  public int plusOne() {
    return helper(value);
  }
}
