package examples.java.receiver.withshouldcontinueandinit;

import edu.illinois.cs.cs125.jenisol.core.Initializer;

public class Parent {
  private int value;

  @Initializer
  private void setInt(int setValue) {
    value = setValue;
  }
}
