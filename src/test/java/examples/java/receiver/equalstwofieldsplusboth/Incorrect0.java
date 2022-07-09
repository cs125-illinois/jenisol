package examples.java.receiver.equalstwofieldsplusboth;

public class Incorrect0 extends Parent {
  private final int first;
  private final int second;

  public Incorrect0(int setFirst, int setSecond) {
    super("correctish");
    first = setFirst;
    second = setSecond;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Incorrect0 correct)) {
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
}
