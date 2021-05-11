package examples.java.receiver.privateconstructorexample;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class Correct {
  private final String department;
  private final String number;

  private Correct(String setDepartment, String setNumber) {
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

  public static Correct[] fromCSV(String csv) {
    assert csv != null;
    String[] lines = csv.split("\n");
    Correct[] courses = new Correct[lines.length];
    for (int i = 0; i < lines.length; i++) {
      String[] parts = lines[i].split(",");
      courses[i] = new Correct(parts[0].trim(), parts[1].trim());
    }
    return courses;
  }

  @FixedParameters
  private static final List<One<String>> SIMPLE =
      Arrays.asList(new One<>("CS,125"), new One<>(null));

  @RandomParameters
  private static One<String> randomCSV(int complexity, Random random) {
    int length = random.nextInt(4);
    String[] parts = new String[length];
    for (int i = 0; i < length; i++) {
      String department = "blah";
      String number = "ee";
      parts[i] =
          " ".repeat(random.nextInt(4))
              + department
              + "  "
              + " ".repeat(random.nextInt(4))
              + ","
              + " ".repeat(random.nextInt(4))
              + number
              + " ".repeat(random.nextInt(4));
    }
    return new One<>(String.join("\n", parts));
  }
}
