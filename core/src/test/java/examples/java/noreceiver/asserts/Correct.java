package examples.java.noreceiver.asserts;

public class Correct {
  public static int value(int first) {
    assert first >= 0;
    return first + 1;
  }
}
