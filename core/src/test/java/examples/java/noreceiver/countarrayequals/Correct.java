package examples.java.noreceiver.countarrayequals;

public class Correct {
  public int value(int[][] values, int check) {
    int count = 0;
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values[i].length; j++) {
        if (values[i][j] == check) {
          count++;
        }
      }
    }
    return count;
  }
}
