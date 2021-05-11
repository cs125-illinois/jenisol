package examples.java.noreceiver.mapreturn;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.One;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Correct {
  public static Map<Integer, Double> invert(Map<Double, Integer> input) {
    assert input != null;

    Map<Integer, Double> returnMap = new HashMap<>();
    for (Double key : input.keySet()) {
      returnMap.put(input.get(key), key);
    }
    return returnMap;
  }

  @FixedParameters
  private static final List<One<Map<Double, Integer>>> FIXED =
      Arrays.asList(
          new One<>(Map.ofEntries(Map.entry(1.0, 1), Map.entry(2.0, 2))), new One<>(null));

  @RandomParameters
  private static One<Map<Double, Integer>> randomParameters(int complexity, Random random) {
    Map<Integer, Boolean> keys = new HashMap<>();
    int size = random.nextInt(63) + 1;
    for (int i = 0; i < size; i++) {
      keys.put(random.nextInt(1024), true);
    }
    Map<Double, Integer> toReturn = new HashMap<>();
    for (Integer key : keys.keySet()) {
      toReturn.put(random.nextDouble(), key);
    }
    return new One<>(toReturn);
  }
}
