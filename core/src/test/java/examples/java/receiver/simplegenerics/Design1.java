package examples.java.receiver.simplegenerics;

public class Design1<T, E> {
  private T value;

  public Design1(T setValue) {
    value = setValue;
  }

  public T getValue() {
    return value;
  }
}
