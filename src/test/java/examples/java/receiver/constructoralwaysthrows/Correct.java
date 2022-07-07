package examples.java.receiver.constructoralwaysthrows;

public class Correct {
  @SuppressWarnings("FieldMayBeFinal")
  private int value;

  public Correct(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }
}
