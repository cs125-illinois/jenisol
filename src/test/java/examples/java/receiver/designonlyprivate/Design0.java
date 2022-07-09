package examples.java.receiver.designonlyprivate;

public class Design0 {
  private final int value;

  public Design0(int setValue) {
    value = setValue;
  }

  private int helping(int newValue) {
    return newValue + 1;
  }

  public int plusOne() {
    return helping(value);
  }
}
