package examples.java.receiver.equals;

public class Incorrect0 {
  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private final int v;

  public Incorrect0(int setValue) {
    v = setValue;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof Incorrect0)) {
      return true;
    }
    Incorrect0 correct = (Incorrect0) o;
    return v == correct.v;
  }
}
