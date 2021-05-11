package examples.java.noreceiver.assertvrequire;

public class Incorrect0 {
  public static int positive(int value) {
    assert value >= 0;
    return value;
  }
}
