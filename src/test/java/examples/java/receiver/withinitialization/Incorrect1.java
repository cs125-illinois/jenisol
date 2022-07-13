package examples.java.receiver.withinitialization;

public class Incorrect1 extends Parent {
  public int getValue() {
    if (value < 2) {
      return value;
    }
    return 0;
  }
}
