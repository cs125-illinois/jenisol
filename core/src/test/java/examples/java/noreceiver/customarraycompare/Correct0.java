package examples.java.noreceiver.customarraycompare;

import edu.illinois.cs.cs125.jenisol.core.Compare;
import edu.illinois.cs.cs125.jenisol.core.CompareException;

public class Correct0 {
  public static int[] returnEven() {
    return new int[] {4};
  }

  @Compare
  private static void compare(int[] solution, int[] submission) {
    if (submission[0] % 2 != 0) {
      throw new CompareException("Should return an even value");
    }
  }
}
