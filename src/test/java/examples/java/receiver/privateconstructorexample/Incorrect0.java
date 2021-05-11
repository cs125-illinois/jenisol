package examples.java.receiver.privateconstructorexample;

public final class Incorrect0 {
  private final String department;
  private final String number;

  private Incorrect0(String setDepartment, String setNumber) {
    assert setDepartment != null;
    assert setNumber != null;
    department = setDepartment;
    number = setNumber;
  }

  public String getDepartment() {
    return department;
  }

  public String getNumber() {
    return number;
  }

  public static Incorrect0[] fromCSV(String csv) {
    assert csv != null;
    String[] lines = csv.split("\n");
    Incorrect0[] courses = new Incorrect0[lines.length];
    for (int i = 0; i < lines.length; i++) {
      String[] parts = lines[i].split(",");
      courses[i] = new Incorrect0(parts[0].trim(), parts[1]);
    }
    return courses;
  }
}
