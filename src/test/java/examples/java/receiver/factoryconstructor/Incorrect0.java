package examples.java.receiver.factoryconstructor;

public class Incorrect0 {
  private int value;

  private Incorrect0(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return 0;
  }

  public static Incorrect0 create(int setValue) {
    return new Incorrect0(setValue);
  }
}
