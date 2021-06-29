package examples.java.noreceiver.kotlinmap;

import java.util.Map;

public class Correct {
  public static int sumValues(Map<String, Integer> map) {
    int sum = 0;
    for (int value : map.values()) {
      sum += value;
    }
    return sum;
  }
}
