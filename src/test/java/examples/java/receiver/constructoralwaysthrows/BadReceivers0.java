package examples.java.receiver.constructoralwaysthrows;

public class BadReceivers0 {
  @SuppressWarnings("FieldMayBeFinal")
  private int value;

  public BadReceivers0(int setValue) {
    throw new IllegalArgumentException();
  }

  public int getValue() {
    return value;
  }
}
