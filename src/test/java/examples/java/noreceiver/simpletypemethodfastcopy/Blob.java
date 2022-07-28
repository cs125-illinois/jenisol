package examples.java.noreceiver.simpletypemethodfastcopy;

import java.util.Objects;

public class Blob {
  private final int value;

  public Blob(int setValue) {
    value = setValue;
  }

  public Blob(Blob other) {
    value = other.value;
  }

  public int getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Blob blob = (Blob) o;
    return value == blob.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
