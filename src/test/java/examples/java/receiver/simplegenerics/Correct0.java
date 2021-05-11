package examples.java.receiver.simplegenerics;

public class Correct0<E> {
  private E value;

  public Correct0(E setValue) {
    value = setValue;
  }

  public E getValue() {
    return value;
  }
}
