package examples.java.noreceiver.systeminadd;

import edu.illinois.cs.cs125.jenisol.core.EdgeType;
import edu.illinois.cs.cs125.jenisol.core.Limit;
import edu.illinois.cs.cs125.jenisol.core.ProvideSystemIn;
import edu.illinois.cs.cs125.jenisol.core.RandomType;
import edu.illinois.cs.cs125.jenisol.core.SimpleType;
import edu.illinois.cs.cs125.jenisol.core.generators.SystemIn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

@SuppressWarnings("StringOperationCanBeSimplified")
public class Correct {
  @Limit(16)
  @ProvideSystemIn
  public static void main() {
    Scanner scanner = new Scanner(System.in);
    int first;
    int second;
    while (true) {
      System.out.println("Enter a number: ");
      try {
        first = scanner.nextInt();
        break;
      } catch (Exception ignored) {
        scanner.next();
      }
    }
    while (true) {
      System.out.println("Enter another number: ");
      try {
        second = scanner.nextInt();
        break;
      } catch (Exception ignored) {
        scanner.next();
      }
    }
    System.out.println("The sum is " + (first + second));
  }

  @SimpleType
  private static final SystemIn[] SIMPLE =
      new SystemIn[] {new SystemIn(Arrays.asList("124", "125"))};

  @EdgeType private static final SystemIn[] EDGE = new SystemIn[] {};

  @RandomType
  private static SystemIn randomInput(Random random) {
    List<String> input = new ArrayList<>();
    int numbers = 0;
    for (int i = 0; i < 32 && numbers < 2; i++) {
      if (random.nextBoolean()) {
        input.add(new String("" + random.nextInt()));
        numbers++;
      } else {
        input.add(new String("" + random.nextDouble()));
      }
    }
    return new SystemIn(input);
  }
}
