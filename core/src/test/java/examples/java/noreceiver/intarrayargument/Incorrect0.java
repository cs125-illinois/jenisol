package examples.java.noreceiver.intarrayargument;

@SuppressWarnings("unused")
public class Incorrect0 {
  public static int value(int[] argument) {
    argument[0] = 0;
    return 0;
  }
}
