package examples.java.receiver.equals;

public class Incorrect3 {
  private final int value;

  public Incorrect3(int setValue) {
    value = setValue;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Incorrect3)) {
      return false;
    }
    return equals(o);
  }
}
