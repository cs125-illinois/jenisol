package examples.java.receiver.kotlinnonnullableparameter;

public class Correct {
  private String value;

  public Correct(String setValue) {
    assert setValue != null;
    value = setValue;
  }

  public String getValue() {
    return value;
  }
}
