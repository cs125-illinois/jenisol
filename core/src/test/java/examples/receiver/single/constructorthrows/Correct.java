package examples.receiver.single.constructorthrows;

public class Correct {
  @SuppressWarnings("FieldMayBeFinal")
  private int value;
  public Correct(int setValue) {
    if (setValue < 0) {
      throw new IllegalArgumentException("setValue should be positive");
    }
    value = setValue;
  }

  public int getValue() {
    return value;
  }
}
