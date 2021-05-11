package examples.java.receiver.equals;

public class Incorrect1 {
  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private final int v;

  public Incorrect1(int setValue) {
    v = setValue;
  }

  @Override
  public boolean equals(Object o) {
    return false;
  }
}
