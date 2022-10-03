package examples.java.receiver.tripleequalswithasserts;

public class Incorrect0 {
  private final String string;
  private final double first;
  private final double second;

  public Incorrect0(String setString, double setFirst, double setSecond) {
    assert setString != null;
    assert setFirst >= -90.0 && setFirst <= 90.0;
    assert setSecond >= -180.0 && setSecond <= 180.0;
    string = setString;
    first = setFirst;
    second = setSecond;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Incorrect0 correct)) {
      return true;
    }
    return first == correct.first && second == correct.second;
  }
}
