package examples.java.receiver.receiverwithtransformer;

public class Incorrect0 {
  private final int value;

  public Incorrect0(int setValue) {
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public static Incorrect0 compare(Incorrect0 first, Incorrect0 second) {
    if (first.value - 1 > second.value) {
      return first;
    } else {
      return second;
    }
  }
}
