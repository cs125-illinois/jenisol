package examples.java.noreceiver.customarraycompare;

import edu.illinois.cs.cs125.jenisol.core.Compare;
import edu.illinois.cs.cs125.jenisol.core.CompareException;

public class Correct {
  public static int[] returnEven() {
    return new int[] {2};
  }

  @Compare
  private static void compare(int[] solution, int[] submission) {
    if (submission[0] % 2 != 0) {
      throw new CompareException("Should return an even value");
    }
  }
}
