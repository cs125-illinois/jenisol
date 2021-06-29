package examples.java.noreceiver.kotlinprintmap;

import java.util.Map;

public class Correct {
  public static void printValues(Map<String, Integer> map) {
    for (int value : map.values()) {
      System.out.println(value);
    }
  }
}
