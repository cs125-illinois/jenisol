package examples.java.receiver.missinginheritance;

@SuppressWarnings("unused")
public class Incorrect0 extends Parent {
  private final int value;

  public Incorrect0(String setType, int setValue) {
    super("oops");
    value = setValue;
  }

  public int getValue() {
    return value;
  }
}
