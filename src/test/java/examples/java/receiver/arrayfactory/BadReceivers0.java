package examples.java.receiver.arrayfactory;

public class BadReceivers0 {
  private int value;

  private BadReceivers0(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public static BadReceivers0[] create() {
    return new BadReceivers0[] {new BadReceivers0(2)};
  }
}
