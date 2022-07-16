package examples.java.receiver.nonstaticparametergenerator;

import edu.illinois.cs.cs125.jenisol.core.NotNull;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Random;

public class Correct {
  private final String string;

  public Correct(@NotNull String setString) {
    string = setString;
  }

  public boolean contains(String test) {
    return test != null && string.contains(test);
  }

  @RandomParameters("contains")
  private String randomString(Random random) {
    if (string == null || string.length() == 0 || random.nextBoolean()) {
      return string;
    } else {
      int one = random.nextInt(string.length());
      int two = random.nextInt(string.length());
      return string.substring(Math.min(one, two), Math.max(one, two));
    }
  }
}
