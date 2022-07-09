package examples.java.receiver.equalstwofieldsplusboth;

import edu.illinois.cs.cs125.jenisol.core.Both;

public class Correct extends Parent {
  private final int first;
  private final int second;

  public Correct(int setFirst, int setSecond) {
    super("correct");
    first = setFirst;
    second = setSecond;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Correct correct)) {
      return false;
    }
    return first == correct.first && second == correct.second;
  }

  public int dangerousness() {
    return first * second;
  }

  public boolean anotherTest() {
    return true;
  }

  @Both
  public static String getType(Parent parent) {
    return parent.getType();
  }
}
