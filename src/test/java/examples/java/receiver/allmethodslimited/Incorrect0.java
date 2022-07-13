package examples.java.receiver.allmethodslimited;

public class Incorrect0 {
  private final int value;

  public Incorrect0(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value + 1;
  }

  public int getAnotherValue() {
    return value - 1;
  }
}
