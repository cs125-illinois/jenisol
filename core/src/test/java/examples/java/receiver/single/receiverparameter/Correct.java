package examples.java.receiver.single.receiverparameter;

public class Correct {
  private int value;
  private boolean used = false;

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
