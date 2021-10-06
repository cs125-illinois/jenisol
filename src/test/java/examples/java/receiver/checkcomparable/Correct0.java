package examples.java.receiver.checkcomparable;

import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.TestResult;
import edu.illinois.cs.cs125.jenisol.core.Verify;

@SuppressWarnings("UseCompareMethod")
public class Correct0 implements Comparable<Correct0> {
  private final String string;

  public Correct0(String setString) {
    assert setString != null;
    string = setString;
  }

  @Override
  public int compareTo(Correct0 other) {
    if (string.length() > other.string.length()) {
      return 2;
    } else if (string.length() < other.string.length()) {
      return -2;
    } else {
      return 0;
    }
  }

  @Verify
  private static void verify(TestResult<Integer, One<Correct0>> results) {
    Throwable solutionThrew = results.solution.threw;
    Throwable submissionThrew = results.submission.threw;
    if (solutionThrew != null) {
      if (submissionThrew == null) {
        results.differs.add(TestResult.Differs.THREW);
      } else if (solutionThrew instanceof AssertionError) {
        if (!((submissionThrew instanceof AssertionError)
            || (submissionThrew instanceof ClassCastException)
            || (submissionThrew instanceof NullPointerException))) {
          results.differs.add(TestResult.Differs.THREW);
        }
      } else if (solutionThrew.getClass() != submissionThrew.getClass()) {
        results.differs.add(TestResult.Differs.THREW);
      }
      return;
    }
    if (submissionThrew != null) {
      results.differs.add(TestResult.Differs.THREW);
      return;
    }
    Integer solutionReturn = results.solution.returned;
    Integer submissionReturn = results.submission.returned;
    if (solutionReturn > 0) {
      if (submissionReturn <= 0) {
        results.differs.add(TestResult.Differs.RETURN);
        results.message = "Submission did not return a positive value";
      }
    } else if (solutionReturn < 0) {
      if (submissionReturn >= 0) {
        results.differs.add(TestResult.Differs.RETURN);
        results.message = "Submission did not return a negative value";
      }
    } else if (submissionReturn != 0) {
      results.differs.add(TestResult.Differs.RETURN);
      results.message = "Submission did not return zero";
    }
  }
}
