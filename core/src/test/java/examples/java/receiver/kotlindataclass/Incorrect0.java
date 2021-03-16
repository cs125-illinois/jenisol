package examples.java.receiver.kotlindataclass;

import java.util.Objects;

public class Incorrect0 {
  private String value;

  public Incorrect0(String setValue) {
    value = setValue;
  }

  public void setValue(String setValue) {
    value = setValue + setValue;
  }

  public String getValue() {
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
    Incorrect0 that = (Incorrect0) o;
    return Objects.equals(value, that.value);
  }
}
