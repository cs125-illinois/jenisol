package examples.java.receiver.equalswithtwofields;

public class Incorrect0 {
  private final double first;
  private final double second;

  public Incorrect0(double setFirst, double setSecond) {
    first = setFirst;
    second = setSecond;
  }

  @Override
  public boolean equals(Object o) {
    return false;
  }
}
