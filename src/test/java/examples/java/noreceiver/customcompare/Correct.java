package examples.java.noreceiver.customcompare;

import edu.illinois.cs.cs125.jenisol.core.Compare;
import edu.illinois.cs.cs125.jenisol.core.CompareException;

public class Correct {
  public static int returnEven() {
    return 2;
  }

  @Compare
  private static void compare(int solution, int submission) {
    if (submission % 2 != 0) {
      throw new CompareException("Should return an even value");
    }
  }
}
