package examples.java.receiver.missinginheritance;

@SuppressWarnings("unused")
public class Design0 {
  private final int value;
  private final String type;

  public Design0(String setType, int setValue) {
    type = setType;
    value = setValue;
  }

  public int getValue() {
    return value;
  }

  public String getType() {
    return type;
  }
}
