package examples.java.noreceiver.sourcechecker;

public class Design0 {
  public static long factorial(long input) {
    if (input < 0 || input > 20) {
      throw new IllegalArgumentException();
    }
    long result = 1;
    for (int i = 2; i <= input; i++) {
      result = result * i;
    }
    return result;
  }
}
