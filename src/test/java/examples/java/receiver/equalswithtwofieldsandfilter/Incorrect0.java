package examples.java.receiver.equalswithtwofieldsandfilter;

public class Incorrect0 {
  private final double first;
  private final double second;

  public Incorrect0(double setFirst, double setSecond) {
    first = setFirst;
    second = setSecond;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Incorrect0)) {
      return true;
    }
    Incorrect0 correct = (Incorrect0) o;
    return first == correct.first && second == correct.second;
  }
}
