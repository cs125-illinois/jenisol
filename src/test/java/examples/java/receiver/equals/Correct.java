package examples.java.receiver.equals;

public class Correct {
  private final int value;

  public Correct(int setValue) {
    value = setValue;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Correct)) {
      return false;
    }
    Correct correct = (Correct) o;
    return value == correct.value;
  }
}
