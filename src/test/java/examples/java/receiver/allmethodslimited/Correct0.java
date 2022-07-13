package examples.java.receiver.allmethodslimited;

public class Correct0 {
  private final int value;
  private int count = 0;
  private int anotherCount = 0;

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
    if (anotherCount++ == 0) {
      return value;
    }
    return value - 1;
  }
}
