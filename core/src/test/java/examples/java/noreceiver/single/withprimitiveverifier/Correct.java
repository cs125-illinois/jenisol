package examples.java.noreceiver.single.withprimitiveverifier;

import edu.illinois.cs.cs125.jenisol.core.None;
import edu.illinois.cs.cs125.jenisol.core.TestResult;
import edu.illinois.cs.cs125.jenisol.core.Verify;

public class Correct {
  public static int value() {
    return 0;
  }

  @SuppressWarnings("ConstantConditions")
  @Verify
  public static void verify(TestResult<Integer, None> results) {
    int solution = results.solution.returned;
    int submission = results.submission.returned;
    if (solution != submission) {
      results.differs.add(TestResult.Differs.RETURN);
    }
  }
}
