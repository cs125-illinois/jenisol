package examples.java.receiver.withshouldcontinue;

import edu.illinois.cs.cs125.jenisol.core.ShouldContinue;

public class Correct {
  @ShouldContinue
  private boolean shouldContinue() {
    return false;
  }

  public int getValue() {
    return 0;
  }
}
