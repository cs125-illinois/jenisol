package examples.receiver.single.receiverparameter;

public class Correct implements CorrectImpl {
  private int value;

  public Correct() {
    value = 0;
  }

  public Correct(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public int inc(Correct other) {
    return other.value++;
  }
}
