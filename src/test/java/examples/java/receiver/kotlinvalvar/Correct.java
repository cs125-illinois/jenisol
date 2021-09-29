package examples.java.receiver.kotlinvalvar;

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
}
