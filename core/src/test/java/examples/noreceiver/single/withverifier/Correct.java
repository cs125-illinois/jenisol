package examples.noreceiver.single.withverifier;

import edu.illinois.cs.cs125.jenisol.core.TestResult;
import edu.illinois.cs.cs125.jenisol.core.Verify;
import java.util.Arrays;

public class Correct {
  public static int[] value() {
    return new int[] {1, 2, 3, 4};
  }

  @SuppressWarnings("ConstantConditions")
  @Verify
  public static void verify(TestResult<int[]> results) {
    int[] solution = results.getSolution().getReturned().clone();
    int[] submission = results.getSubmission().getReturned().clone();
    Arrays.sort(solution);
    Arrays.sort(submission);
    if (!Arrays.equals(solution, submission)) {
      results.getDiffers().add(TestResult.Differs.RETURN);
    }
  }
}
