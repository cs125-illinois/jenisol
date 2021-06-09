package examples.java.noreceiver.twostringequality;

public class Correct {
  public static boolean equals(String first, String second) {
    assert first != null;
    assert second != null;
    return first.equals(second);
  }
}
