package examples.java.noreceiver.arrayfromtypegenerator;

import edu.illinois.cs.cs125.jenisol.core.EdgeType;
import edu.illinois.cs.cs125.jenisol.core.RandomType;
import edu.illinois.cs.cs125.jenisol.core.SimpleType;

import java.util.Random;

public class Correct {
  public static int count(Item[] items) {
    return items.length;
  }

  @SimpleType private static final Item[] ITEMS = {new Item()};
  @EdgeType private static final Item[] EDGES = {null};

  @RandomType
  private static Item randomItem(int complexity, Random random) {
    return new Item();
  }
}
