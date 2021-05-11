package examples.java.noreceiver.assertvrequire;

public class Correct {
  public static int positive(int value) {
    assert value > 0;
    return value;
  }
}
