package examples.noreceiver.single.simpletype;

import edu.illinois.cs.cs125.jenisol.core.SimpleType;

public class Correct {
  @SimpleType public static final int[] SIMPLE = new int[] {8888, 888888};

  public static boolean value(int first) {
    return first == 8888 || first == 888888;
  }
}
