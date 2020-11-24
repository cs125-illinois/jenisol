package examples.java.receiver.simplegenerics;

public class Incorrect0<T> {
  private T value;
  public Incorrect0(T setValue) {
    value = setValue;
  }

  public T getValue() {
    return null;
  }
}
