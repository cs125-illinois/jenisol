package examples.java.receiver.arrayfactory;

public class Correct {
  private int value;

  private Correct(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public static Correct[] create() {
    return new Correct[] {new Correct(1), new Correct(2)};
  }
}
