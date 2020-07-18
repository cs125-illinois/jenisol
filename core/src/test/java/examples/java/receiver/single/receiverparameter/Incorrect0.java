package examples.java.receiver.single.receiverparameter;

public class Incorrect0 {
  private int value;

  public Incorrect0() {
    value = 0;
  }

  public Incorrect0(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public int inc(Incorrect0 other) {
    return other.value;
  }
}
