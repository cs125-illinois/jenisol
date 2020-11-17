package examples.java.receiver.bothonly;

import edu.illinois.cs.cs125.jenisol.core.Both;

public class Correct extends Parent {

  public Correct(String setType) {
    super(setType);
  }

  @Both
  private static String getType(Parent parent) {
    return parent.getType();
  }
}
