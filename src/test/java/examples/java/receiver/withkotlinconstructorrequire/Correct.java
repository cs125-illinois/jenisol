package examples.java.receiver.withkotlinconstructorrequire;

public class Correct {
  private final int value;

  public Correct(int setValue) {
    assert setValue > 0;
    value = setValue;
  }

  public int getValue() {
    return value;
  }
}
