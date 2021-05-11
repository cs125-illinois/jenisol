package examples.java.receiver.factoryconstructor;

public class Correct {
  private int value;

  private Correct(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public static Correct create(int setValue) {
    return new Correct(setValue);
  }
}
