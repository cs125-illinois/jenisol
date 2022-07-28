package examples.java.noreceiver.simpletypemethodfastcopy;

import edu.illinois.cs.cs125.jenisol.core.EdgeType;
import edu.illinois.cs.cs125.jenisol.core.RandomType;
import edu.illinois.cs.cs125.jenisol.core.SimpleType;
import java.util.Random;

public class Correct {
  public static int get(Blob blob) {
    return blob.getValue();
  }

  @SimpleType
  private static Blob[] simpleBlob() {
    return new Blob[] {new Blob(0)};
  }

  @EdgeType private static final Blob[] EDGE = new Blob[] {};

  @RandomType
  private static Blob randomBlob(int complexity, Random random) {
    return new Blob(random.nextInt());
  }
}
