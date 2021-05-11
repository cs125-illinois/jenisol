package examples.java.noreceiver.countarrayequals2d;

public class Incorrect1 {
  public int value(int[][] values, int check) {
    int count = 0;
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values[i].length; j++) {
        if (values[i][j] == check) {
          return 1;
        }
      }
    }
    return 0;
  }
}
