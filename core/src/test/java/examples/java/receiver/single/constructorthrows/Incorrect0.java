package examples.java.receiver.single.constructorthrows;

public class Incorrect0 {
  @SuppressWarnings("FieldMayBeFinal")
  private int value;

  public Incorrect0(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }
}
