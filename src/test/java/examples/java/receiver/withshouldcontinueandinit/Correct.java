package examples.java.receiver.withshouldcontinueandinit;

import edu.illinois.cs.cs125.jenisol.core.ShouldContinue;

public class Correct extends Parent {
  @ShouldContinue
  private boolean shouldContinue() {
    return false;
  }

  public int getValue() {
    return 0;
  }
}
