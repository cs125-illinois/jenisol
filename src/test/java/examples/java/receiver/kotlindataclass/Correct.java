package examples.java.receiver.kotlindataclass;

import java.util.Objects;

public class Correct {
  private String value;

  public Correct(String setValue) {
    value = setValue;
  }

  public void setValue(String setValue) {
    value = setValue;
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
    Correct correct = (Correct) o;
    return Objects.equals(value, correct.value);
  }
}
