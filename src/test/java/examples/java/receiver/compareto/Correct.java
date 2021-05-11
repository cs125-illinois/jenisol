package examples.java.receiver.compareto;

import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.TestResult;
import edu.illinois.cs.cs125.jenisol.core.Verify;

@SuppressWarnings({"rawtypes", "NullableProblems", "ConstantConditions"})
public class Correct implements Comparable {
  private final String string;

  public Correct(String setString) {
    assert setString != null;
    string = setString;
  }

  @Override
  public int compareTo(Object o) {
    assert o instanceof Correct;
    Correct other = (Correct) o;
    return Integer.compare(string.length(), other.string.length());
  }

  @Verify
  private static void verify(TestResult<Integer, One<Object>> results) {
    Throwable solutionThrew = results.solution.threw;
    Throwable submissionThrew = results.submission.threw;
    if (solutionThrew != null) {
      if (submissionThrew == null) {
        results.differs.add(TestResult.Differs.THREW);
      } else if (solutionThrew instanceof AssertionError) {
        if (!(submissionThrew instanceof AssertionError
            || submissionThrew instanceof ClassCastException
            || submissionThrew instanceof NullPointerException)) {
          results.differs.add(TestResult.Differs.THREW);
        }
      } else if (solutionThrew.getClass() != submissionThrew.getClass()) {
        results.differs.add(TestResult.Differs.THREW);
      }
      return;
    }

    int solutionReturn = results.solution.returned;
    int submissionReturn = results.submission.returned;
    if (solutionReturn > 0) {
      if (submissionReturn <= 0) {
        results.differs.add(TestResult.Differs.RETURN);
        results.message = "Submission did not return a positive value";
      }
    } else if (solutionReturn < 0) {
      if (submissionReturn > 0) {
        results.differs.add(TestResult.Differs.RETURN);
        results.message = "Submission did not return a negative value";
      }
    } else if (submissionReturn != 0) {
      results.differs.add(TestResult.Differs.RETURN);
      results.message = "Submission did not return zero";
    }
  }
}
