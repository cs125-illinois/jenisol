package examples.java.noreceiver.withgenericverifier;

import edu.illinois.cs.cs125.jenisol.core.None;
import edu.illinois.cs.cs125.jenisol.core.TestResult;
import edu.illinois.cs.cs125.jenisol.core.Verify;
import java.util.Arrays;
import java.util.List;

public class Correct {
  public static List<Integer> value() {
    return Arrays.asList(1, 2, 3, 4);
  }

  @SuppressWarnings("ConstantConditions")
  @Verify
  public static void verify(TestResult<List<Integer>, None> results) {
    Integer[] solution = results.solution.returned.toArray(new Integer[0]);
    Integer[] submission = results.submission.returned.toArray(new Integer[0]);
    Arrays.sort(solution);
    Arrays.sort(submission);
    if (!Arrays.equals(solution, submission)) {
      results.differs.add(TestResult.Differs.RETURN);
    }
  }
}
