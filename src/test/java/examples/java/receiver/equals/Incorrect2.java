package examples.java.receiver.equals;

public class Incorrect2 {
  @SuppressWarnings({"FieldCanBeLocal"})
  private final int v;

  public Incorrect2(int setValue) {
    v = setValue;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    Incorrect2 correct = (Incorrect2) o;
    return v == correct.v;
  }
}
