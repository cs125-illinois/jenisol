package examples.java.receiver.equals;

public class Incorrect0 {
  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private final int value;

  public Incorrect0(int setValue) {
    value = setValue;
  }

  @Override
  public boolean equals(Object o) {
    return false;
  }
}
