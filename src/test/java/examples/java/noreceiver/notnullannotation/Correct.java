package examples.java.noreceiver.notnullannotation;

import edu.illinois.cs.cs125.jenisol.core.NotNull;

public class Correct {
  public static int it(@NotNull String input) {
    if (input == null) {
      return 0;
    } else {
      return input.length();
    }
  }
}
