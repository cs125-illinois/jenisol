package examples.java.receiver.withinitialization;

public class Incorrect1 extends Parent {
  public int getValue() {
    if (value < 5) {
      return value;
    }
    return 0;
  }
}
