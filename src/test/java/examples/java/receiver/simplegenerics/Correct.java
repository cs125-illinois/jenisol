package examples.java.receiver.simplegenerics;

public class Correct<T> {
  private T value;

  public Correct(T setValue) {
    value = setValue;
  }

  public T getValue() {
    return value;
  }
}
