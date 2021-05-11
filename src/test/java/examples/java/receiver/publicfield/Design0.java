package examples.java.receiver.publicfield;

public class Design0 {
  @SuppressWarnings("checkstyle:VisibilityModifier")
  public int value = 0;

  public int increment() {
    return value++;
  }
}
