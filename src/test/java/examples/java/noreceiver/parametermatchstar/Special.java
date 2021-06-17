package examples.java.noreceiver.parametermatchstar;

import java.util.Objects;

public class Special {
  private final int first;
  private final String second;

  public Special(int setFirst, String setSecond) {
    first = setFirst;
    second = setSecond;
  }

  public int getFirst() {
    return first;
  }

  public String getSecond() {
    return second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Special special = (Special) o;
    return first == special.first && Objects.equals(second, special.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }
}
