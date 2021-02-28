package examples.java.receiver.doubleassert;

public class Incorrect0 {
  private String name;
  private int enrollment;

  public Incorrect0(String setName, int setEnrollment) {
    assert setEnrollment >= 0;
    name = setName;
    enrollment = setEnrollment;
  }

  public int getEnrollment() {
    return enrollment;
  }

  public void setEnrollment(int setEnrollment) {
    assert setEnrollment >= 0;
    enrollment = setEnrollment;
  }

  public String getName() {
    return name;
  }
}
