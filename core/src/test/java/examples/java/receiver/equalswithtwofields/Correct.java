package examples.java.receiver.equalswithtwofields;

public class Correct {
  private final double first;
  private final double second;

  public Correct(double setFirst, double setSecond) {
    first = setFirst;
    second = setSecond;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Correct)) {
      return false;
    }
    Correct correct = (Correct) o;
    return first == correct.first && second == correct.second;
  }
}
