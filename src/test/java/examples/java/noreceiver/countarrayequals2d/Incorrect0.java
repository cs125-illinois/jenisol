package examples.java.noreceiver.countarrayequals2d;

import java.util.Arrays;

public class Incorrect0 {
  public int value(int[][] values, int check) {
    int count = 0;
    for (int i = 0; i < values.length; i++) {
      System.out.println(Arrays.toString(values[i]));
      for (int j = 0; j < values[i].length; j++) {
        count++;
      }
    }
    return count;
  }
}
