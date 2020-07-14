package examples.receiver.single.constructorthrows;

public class Incorrect1 {
  @SuppressWarnings("FieldMayBeFinal")
  private int value;
  public Incorrect1(int setValue) {
    if (setValue < 0) {
      throw new IllegalArgumentException("setValue should be positive");
    }
    value = setValue;
  }

  public int getValue() {
    return value + 1;
  }
}
