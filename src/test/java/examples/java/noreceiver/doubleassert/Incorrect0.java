package examples.java.noreceiver.doubleassert;

public class Incorrect0 {
  public String doubleAssert(String name, int enrollment) {
    assert enrollment >= 0;
    return name;
  }
}
