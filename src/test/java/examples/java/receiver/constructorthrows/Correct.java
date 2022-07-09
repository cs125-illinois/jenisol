package examples.java.receiver.constructorthrows;

public class Correct {
  @SuppressWarnings("FieldMayBeFinal")
  private int value;

  public Correct(int setValue) {
    if (setValue < 0) {
      throw new IllegalArgumentException();
    }
    value = setValue;
  }

  public int getValue() {
    return value;
  }
}
