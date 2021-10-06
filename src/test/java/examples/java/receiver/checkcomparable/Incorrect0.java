package examples.java.receiver.checkcomparable;

@SuppressWarnings("UseCompareMethod")
public class Incorrect0 implements Comparable<Incorrect0> {
  private final String string;

  public Incorrect0(String setString) {
    assert setString != null;
    string = setString;
  }

  @Override
  public int compareTo(Incorrect0 other) {
    if (string.length() > other.string.length()) {
      return -1;
    } else if (string.length() < other.string.length()) {
      return 1;
    } else {
      return 0;
    }
  }
}
