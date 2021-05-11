package examples.java.noreceiver.countarrayequals1d;

import java.util.Arrays;

public class Incorrect0 {
  public int value(int[] values, int check) {
    System.out.println(Arrays.toString(values));
    int count = 0;
    for (int i = 0; i < values.length; i++) {
      if (values[i] == check) {
        return 1;
      }
    }
    return 0;
  }
}
