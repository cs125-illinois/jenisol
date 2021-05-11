package examples.java.noreceiver.kotlinrawtypes;

import java.util.Objects;

public class Tester<T> {
  private final T value;

  public Tester(T setValue) {
    value = setValue;
  }

  public T getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Tester<?> tester = (Tester<?>) o;
    return Objects.equals(value, tester.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
