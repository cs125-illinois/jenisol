package examples.java.receiver.withinitialization;

import edu.illinois.cs.cs125.jenisol.core.Initializer;

public class Parent {
  @SuppressWarnings("checkstyle:VisibilityModifier")
  protected int value = 0;

  @Initializer
  private void setValue(int setValue) {
    value = setValue;
  }
}
