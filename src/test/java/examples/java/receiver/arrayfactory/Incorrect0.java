package examples.java.receiver.arrayfactory;

public class Incorrect0 {
  private int value;

  private Incorrect0(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public static Incorrect0[] create() {
    return new Incorrect0[] {new Incorrect0(2)};
  }
}
