package examples.java.noreceiver.simpletypemethodslowcopy;

import edu.illinois.cs.cs125.jenisol.core.EdgeType;
import edu.illinois.cs.cs125.jenisol.core.RandomType;
import edu.illinois.cs.cs125.jenisol.core.SimpleType;
import java.util.Random;

public class Correct {
  public static int get(Blob blob) {
    return blob.getValue();
  }

  @SimpleType(fastCopy = false)
  private static Blob[] simpleBlob() {
    return new Blob[] {new Blob(0)};
  }

  @EdgeType private static final Blob[] EDGE = new Blob[] {};

  @RandomType(fastCopy = false)
  private static Blob randomBlob(int complexity, Random random) {
    return new Blob(random.nextInt());
  }
}
