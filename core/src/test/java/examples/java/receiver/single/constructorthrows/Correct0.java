package examples.java.receiver.single.constructorthrows;

@SuppressWarnings("unused")
public class Correct0 {
  @SuppressWarnings("FieldMayBeFinal")
  private int value;

  public Correct0(int setValue) {
    if (setValue < 0) {
      throw new IllegalArgumentException("setValue should not be negative");
    }
    value = setValue;
  }

  public int getValue() {
    return value;
  }
}
