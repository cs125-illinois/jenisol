package examples.java.receiver.withkotlinconstructorrequire;

public class Incorrect0 {
  private final int value;

  public Incorrect0(int setValue) {
    assert setValue > 0;
    value = setValue;
  }

  public int getValue() {
    return value + 1;
  }
}
