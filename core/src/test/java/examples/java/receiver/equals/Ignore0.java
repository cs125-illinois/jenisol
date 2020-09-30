package examples.java.receiver.equals;

public class Ignore0 {
  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private final int v;

  public Ignore0(int setValue) {
    v = setValue;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof Ignore0)) {
      return true;
    }
    Ignore0 correct = (Ignore0) o;
    return v == correct.v;
  }
}
