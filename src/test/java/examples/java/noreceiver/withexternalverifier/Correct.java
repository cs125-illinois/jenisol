package examples.java.noreceiver.withexternalverifier;

import com.example.VerifiersKt;
import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.TestResult;
import edu.illinois.cs.cs125.jenisol.core.Verify;

public class Correct {
  public static int[] value(int n) {
    return new int[] {1, 2, 3, n};
  }

  @Verify
  private static void verify(TestResult<int[], One<Integer>> results) {
    VerifiersKt.verify(results);
  }
}
