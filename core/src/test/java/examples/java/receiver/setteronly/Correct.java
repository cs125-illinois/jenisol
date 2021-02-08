package examples.java.receiver.setteronly;

public class Correct {
  private int value;

  public void setValue(int setValue) {
    value = setValue;
  }

  public int squared() {
    return value * value;
  }
}
