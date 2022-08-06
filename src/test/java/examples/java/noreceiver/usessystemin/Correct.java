package examples.java.noreceiver.usessystemin;

import edu.illinois.cs.cs125.jenisol.core.EdgeType;
import edu.illinois.cs.cs125.jenisol.core.ProvideSystemIn;
import edu.illinois.cs.cs125.jenisol.core.RandomType;
import edu.illinois.cs.cs125.jenisol.core.SimpleType;
import edu.illinois.cs.cs125.jenisol.core.generators.SystemIn;
import java.util.Random;
import java.util.Scanner;

@SuppressWarnings("StringOperationCanBeSimplified")
public class Correct {
  @ProvideSystemIn
  public static void echo() {
    Scanner scanner = new Scanner(System.in);
    System.out.println(scanner.nextLine());
  }

  @SimpleType private static final SystemIn[] SIMPLE = new SystemIn[] {new SystemIn("Test")};

  @EdgeType private static final SystemIn[] EDGE = new SystemIn[] {};

  @RandomType
  private static SystemIn randomInput(Random random) {
    return new SystemIn("" + random.nextInt());
  }
}
