package examples.java.noreceiver.factoryconstructor;

@SuppressWarnings({"unused", "FinalClass", "FieldCanBeLocal"})
public class Correct {
  private final int value;

  private Correct(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public static Correct value() {
    return new Correct(0);
  }
}
