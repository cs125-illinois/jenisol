package examples.java.noreceiver.filtertest;

public class Correct0 {
  public static int value(int first) {
    if (first % 2 == 0) {
      return first;
    }
    return first + 1;
  }
}
