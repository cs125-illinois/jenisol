package examples.java.noreceiver.voidverify;

import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.TestResult;
import edu.illinois.cs.cs125.jenisol.core.Verify;
import java.util.Arrays;

public class Correct {
  public static void setZero(int[] array) {
    array[0] = 0;
  }

  @Verify
  private static void verify(TestResult<Void, One<int[]>> results) {
    int[] solution = results.solution.parameters.first;
    int[] submission = results.submission.parameters.first;
    if (!Arrays.equals(solution, submission)) {
      results.differs.add(TestResult.Differs.RETURN);
    }
  }
}
