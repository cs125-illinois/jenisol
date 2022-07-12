package examples.java.receiver.finalclass;

public class Incorrect {
  private final int value;

  public Incorrect(int setValue) {
    value = setValue + 1;
  }

  public int getValue() {
    return value;
  }
}
