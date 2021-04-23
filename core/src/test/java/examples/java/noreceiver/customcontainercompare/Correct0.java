package examples.java.noreceiver.customcontainercompare;

import edu.illinois.cs.cs125.jenisol.core.Compare;
import edu.illinois.cs.cs125.jenisol.core.CompareException;
import java.util.Arrays;
import java.util.List;

public class Correct0 {
  public static List<Integer> returnEven() {
    return Arrays.asList(4);
  }

  @Compare
  private static void compare(List<Integer> solution, List<Integer> submission) {
    if (submission.get(0) % 2 != 0) {
      throw new CompareException("Should return an even value");
    }
  }
}
