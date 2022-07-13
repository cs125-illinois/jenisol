package examples.java.receiver.limitwithreceivers;

public class Incorrect0 {
  private final int value;

  public Incorrect0(int setValue) {
    value = setValue;
  }

  public int getValue() {
    if (Math.abs(value) < 8) {
      return value;
    }
    return value + 1;
  }
}
