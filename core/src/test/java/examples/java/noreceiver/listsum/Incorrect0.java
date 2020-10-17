package examples.java.noreceiver.listsum;

import java.util.List;

public class Incorrect0 {
  public static int sum(List<Integer> values) {
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
