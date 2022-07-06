package examples.java.receiver.arrayfactory;

public class Incorrect1 {
  private int value;

  private Incorrect1(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public static Incorrect1[] create() {
    return new Incorrect1[] {new Incorrect1(1), new Incorrect1(3)};
  }
}
