package examples.java.noreceiver.twostringequality;

@SuppressWarnings("StringEquality")
public class Incorrect0 {
  public static boolean equals(String first, String second) {
    assert first != null;
    assert second != null;
    return first == second;
  }
}
