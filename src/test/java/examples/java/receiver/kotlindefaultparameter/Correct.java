package examples.java.receiver.kotlindefaultparameter;

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
