package examples.java.noreceiver.fauxstaticprints;

import edu.illinois.cs.cs125.jenisol.core.Configure;

public class Correct {
  @Configure(strictOutput = true)
  void print() {
    System.out.println("Here");
  }
}
