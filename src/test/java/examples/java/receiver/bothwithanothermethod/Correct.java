package examples.java.receiver.bothwithanothermethod;

import edu.illinois.cs.cs125.jenisol.core.Both;

public class Correct extends Counter {
  public int method() {
    increment();
    return 0;
  }

  @Both
  private static int count(Counter counter) {
    return counter.getCounter();
  }
}
