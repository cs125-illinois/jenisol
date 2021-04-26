package examples.java.noreceiver.withparameterverifier;

import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.TestResult;
import edu.illinois.cs.cs125.jenisol.core.Verify;

@SuppressWarnings({"unused", "ParameterCanBeLocal"})
public class Correct {
  public static int both(int[] values) {
    values = new int[] {0};
    return values.length;
  }

  @SuppressWarnings("ConstantConditions")
  @Verify
  private static void verify(TestResult<Integer, One<int[]>> results) {
    int solutionReturn = results.solution.returned;
    int submissionReturn = results.submission.returned;
    if (solutionReturn != submissionReturn) {
      results.differs.add(TestResult.Differs.RETURN);
      return;
    }
    int[] solutionArray = results.solution.parameters.first;
    int[] submissionArray = results.submission.parameters.first;
    if (solutionArray != null && solutionArray.length != submissionArray.length) {
      results.differs.add(TestResult.Differs.PARAMETERS);
    }
  }
}
