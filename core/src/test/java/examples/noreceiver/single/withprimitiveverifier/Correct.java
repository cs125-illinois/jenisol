package examples.noreceiver.single.withprimitiveverifier;

import edu.illinois.cs.cs125.jenisol.core.TestResult;
import edu.illinois.cs.cs125.jenisol.core.Verify;

public class Correct {
  public static int value() {
    return 0;
  }

  @SuppressWarnings("ConstantConditions")
  @Verify
  public static void verify(TestResult<Integer> results) {
    int solution = results.getSolution().getReturned();
    int submission = results.getSubmission().getReturned();
    if (solution != submission) {
      results.getDiffers().add(TestResult.Differs.RETURN);
    }
  }
}
