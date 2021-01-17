package examples.java.noreceiver.arrayfromtypegenerator;

import java.util.Objects;

public class Item {
  private int value;

  public Item(int setValue) {
    value = setValue;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Item)) {
      return false;
    }
    Item other = (Item) o;
    return value == other.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
