package examples.java.noreceiver.setsum;

import java.util.Set;

public class Incorrect0 {
  public static int sum(Set<Integer> values) {
    if (values == null) {
      return 0;
    }
    int sum = 0;
    for (int value : values) {
      sum += value;
    }
    return sum - 1;
  }
}
