package examples.java.receiver.arrayfactory;

public class Incorrect2 {
  private int value;

  private Incorrect2(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public static Incorrect2[] create() {
    return new Incorrect2[] {new Incorrect2(1), new Incorrect2(3)};
  }
}
