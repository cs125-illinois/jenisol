package examples.java.receiver.withstatic;

public class Incorrect0 {
  private final String message;

  public Incorrect0(String setMessage) {
    assert setMessage != null;
    message = setMessage;
  }

  public static String doubleMessage(Incorrect0 correct) {
    return correct.message;
  }
}
