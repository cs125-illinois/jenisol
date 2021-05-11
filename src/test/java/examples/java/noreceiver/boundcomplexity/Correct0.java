package examples.java.noreceiver.boundcomplexity;

public class Correct0 {
  public static int value(int first) {
    if (first > 1024) {
      return first;
    }
    return first + 1;
  }
}
