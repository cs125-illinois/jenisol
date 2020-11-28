package examples.java.noreceiver.voidverify;

import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.TestResult;
import edu.illinois.cs.cs125.jenisol.core.Verify;

import java.util.Objects;

public class Correct {
  public static void setZero(int[] array) {
    array[0] = 0;
    return;
  }

  @Verify
  private static void verify(TestResult<Void, One<Integer[]>> results) {
    Integer[] solution = results.solution.parameters.first;
    Integer[] submission = results.submission.parameters.first;
    if (!Objects.equals(solution, submission)) {
      results.differs.add(TestResult.Differs.RETURN);
    }
  }
}
