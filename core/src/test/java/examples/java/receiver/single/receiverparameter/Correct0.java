package examples.java.receiver.single.receiverparameter;

public class Correct0 implements CorrectImpl {
  private int value;

  public Correct0() {
    value = 0;
  }

  public Correct0(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public int inc(Correct0 other) {
    return other.value++;
  }
}
