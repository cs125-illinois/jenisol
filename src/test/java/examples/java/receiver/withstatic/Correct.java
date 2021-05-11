package examples.java.receiver.withstatic;

public class Correct {
  private final String message;

  public Correct(String setMessage) {
    assert setMessage != null;
    message = setMessage;
  }

  public static String doubleMessage(Correct correct) {
    return correct.message + correct.message;
  }
}
