package examples.java.receiver.withinitialization;

public class Incorrect1 extends Parent {
  public int getValue() {
    if (Math.abs(value) < 1024) {
      return value;
    }
    return 0;
  }
}
