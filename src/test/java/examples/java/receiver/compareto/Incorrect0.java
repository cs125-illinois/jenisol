package examples.java.receiver.compareto;

@SuppressWarnings({"rawtypes", "NullableProblems"})
public class Incorrect0 implements Comparable {
  private final String string;

  public Incorrect0(String setString) {
    string = setString;
  }

  @Override
  public int compareTo(Object o) {
    assert o instanceof Incorrect0;
    Incorrect0 other = (Incorrect0) o;
    return Integer.compare(string.length(), other.string.length());
  }
}
