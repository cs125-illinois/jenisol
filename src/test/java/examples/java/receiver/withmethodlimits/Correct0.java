package examples.java.receiver.withmethodlimits;

public class Correct0 {
  private final int value;
  private int count = 0;

  public Correct0(int setValue) {
    value = setValue;
  }

  public int getValue() {
    if (count++ == 0) {
      return value;
    }
    return value + 1;
  }

  public int getAnotherValue() {
    return value;
  }
}
