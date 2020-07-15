package examples.java.submissiondesign;

@SuppressWarnings("unused")
public class Correct {
  @SuppressWarnings("FieldCanBeLocal")
  private final int value;

  public Correct(int setValue) {
    value = setValue;
  }

  public void example() {}

  private double ignored() {
    return 0.0;
  }
}
