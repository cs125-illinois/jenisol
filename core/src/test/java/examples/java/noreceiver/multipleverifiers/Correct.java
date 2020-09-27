package examples.java.noreceiver.multipleverifiers;

import edu.illinois.cs.cs125.jenisol.core.None;
import edu.illinois.cs.cs125.jenisol.core.TestResult;
import edu.illinois.cs.cs125.jenisol.core.Verify;
import java.util.Arrays;

public class Correct {
  public static int[] valueInt() {
    return new int[] {1, 2, 3, 4};
  }

  public static double[] valueDouble() {
    return new double[] {1.0, 2.0, 3.0, 4.0};
  }

  @SuppressWarnings("ConstantConditions")
  @Verify
  public static void verifyInt(TestResult<int[], None> results) {
    int[] solution = results.solution.returned.clone();
    int[] submission = results.submission.returned.clone();
    Arrays.sort(solution);
    Arrays.sort(submission);
    if (!Arrays.equals(solution, submission)) {
      results.differs.add(TestResult.Differs.RETURN);
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Verify
  public static void verifyDouble(TestResult<double[], None> results) {
    double[] solution = results.solution.returned.clone();
    double[] submission = results.submission.returned.clone();
    Arrays.sort(solution);
    Arrays.sort(submission);
    if (!Arrays.equals(solution, submission)) {
      results.differs.add(TestResult.Differs.RETURN);
    }
  }
}
