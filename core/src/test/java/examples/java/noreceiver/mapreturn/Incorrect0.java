package examples.java.noreceiver.mapreturn;

import java.util.HashMap;
import java.util.Map;

public class Incorrect0 {
  public static Map<Integer, Double> invert(Map<Double, Integer> input) {
    assert input != null;

    Map<Integer, Double> returnMap = new HashMap<>();
    for (Double key : input.keySet()) {
      returnMap.put(input.get(key), key + 1);
    }
    return returnMap;
  }
}
