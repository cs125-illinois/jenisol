package examples.java.noreceiver.doubleassert;

public class Correct {
  public String doubleAssert(String name, int enrollment) {
    assert name != null;
    assert enrollment >= 0;
    return name;
  }
}
