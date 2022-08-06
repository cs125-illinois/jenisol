package examples.java.noreceiver.systemininterleaving;

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
    String first = scanner.nextLine();
    System.out.println("First: " + first);
    String second = scanner.nextLine();
    System.out.println("Second: " + first);
  }

  @SimpleType private static final SystemIn[] SIMPLE = new SystemIn[] {new SystemIn("Test\nMe\n")};

  @EdgeType private static final SystemIn[] EDGE = new SystemIn[] {new SystemIn("")};

  @RandomType
  private static SystemIn randomInput(Random random) {
    return new SystemIn(new String("" + random.nextInt() + "\n" + random.nextInt()));
  }
}
