package examples.noreceiver.single.withparameterverifier;

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
  public static void verify(TestResult<Integer, One<Integer[]>> results) {
    int solutionReturn = results.solution.returned;
    int submissionReturn = results.submission.returned;
    if (solutionReturn != submissionReturn) {
      results.differs.add(TestResult.Differs.RETURN);
      return;
    }
    Integer[] solutionArray = results.solution.parameters.first;
    Integer[] submissionArray = results.submission.parameters.first;
    if (solutionArray.length != submissionArray.length) {
      results.differs.add(TestResult.Differs.PARAMETERS);
    }
  }
}
