package examples.noreceiver.single.onegeneratedparameter;

import java.util.Objects;

@SuppressWarnings("unused")
public class Generated {
  private final int value;

  Generated(int setValue) {
    value = setValue;
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
    Generated generated = (Generated) o;
    return value == generated.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
