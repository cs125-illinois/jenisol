package examples.java.noreceiver.lambdaargument;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Correct {
  public static int generate(Generator generator) {
    return generator.generate();
  }

  @FixedParameters
  private static final List<One<Generator>> FIXED = Collections.singletonList(new One<>(() -> 0));

  @RandomParameters(fastCopy = false)
  private static One<Generator> randomParameters(int complexity, Random random) {
    int value = random.nextInt();
    return new One<>(() -> value);
  }
}
