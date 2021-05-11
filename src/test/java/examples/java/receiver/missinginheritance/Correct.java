package examples.java.receiver.missinginheritance;

import edu.illinois.cs.cs125.jenisol.core.Both;

public class Correct extends Parent {
  private final int value;

  public Correct(String setType, int setValue) {
    super(setType);
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  @Both
  public static String getType(Parent parent) {
    return parent.getType();
  }
}
