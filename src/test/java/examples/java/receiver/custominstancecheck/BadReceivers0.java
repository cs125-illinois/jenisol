package examples.java.receiver.custominstancecheck;

public class BadReceivers0 {
  private int[] values;

  public BadReceivers0() {
    values = new int[32];
  }

  public int getValue() {
    return 1;
  }
}
