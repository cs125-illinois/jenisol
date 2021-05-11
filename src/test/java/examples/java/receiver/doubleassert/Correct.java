package examples.java.receiver.doubleassert;

public class Correct {
  private String name;
  private int enrollment;

  public Correct(String setName, int setEnrollment) {
    assert setEnrollment >= 0;
    assert setName != null;
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
