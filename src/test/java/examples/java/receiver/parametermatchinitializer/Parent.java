package examples.java.receiver.parametermatchinitializer;

import edu.illinois.cs.cs125.jenisol.core.Initializer;

public class Parent {
  private int value = 0;

  protected int getValue() {
    return value;
  }

  @Initializer
  private void setValue(int setValue) {
    value = setValue;
  }
}
