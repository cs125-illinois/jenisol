package examples.java.receiver.setteronly;

public class Incorrect0 {
  private int value;

  public void setValue(int setValue) {
    value = setValue;
  }

  public int squared() {
    return value * value + 1;
  }
}
