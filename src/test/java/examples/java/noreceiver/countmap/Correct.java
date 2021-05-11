package examples.java.noreceiver.countmap;

import java.util.Map;

public class Correct {
  public static int count(Map<String, Integer> map) {
    return map.keySet().size();
  }
}
