package examples.java.noreceiver.doublecomparison;

public class Correct0 {
  public double sum(double[] values) {
    double sum = 0.0;
    for (int i = values.length - 1; i >= 0; i--) {
      sum += values[i];
    }
    return sum;
  }
}
