package examples.java.noreceiver.strictoutput;

import edu.illinois.cs.cs125.jenisol.core.Configure;

public class Correct {
  @Configure(strictOutput = true)
  public static void printSometimes(boolean sometimes) {
    if (sometimes) {
      System.out.println("Printed");
    }
  }
}
