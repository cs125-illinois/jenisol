package examples.java.noreceiver.javalistwithquestiontype;

import edu.illinois.cs.cs125.jenisol.core.Compare;
import edu.illinois.cs.cs125.jenisol.core.CompareException;
import java.util.List;

@SuppressWarnings("UseCompareMethod")
public class Correct0 {
  public static int compare(List<?> first, List<?> second) {
    if (first.size() < second.size()) {
      return -2;
    } else if (first.size() > second.size()) {
      return 2;
    } else {
      return 0;
    }
  }

  @Compare
  private static void compare(int solution, int submission) {
    if (solution == 0 && submission != 0) {
      throw new CompareException();
    } else if (solution < 0 && submission >= 0) {
      throw new CompareException();
    } else if (solution > 0 && submission <= 0) {
      throw new CompareException();
    }
  }
}
