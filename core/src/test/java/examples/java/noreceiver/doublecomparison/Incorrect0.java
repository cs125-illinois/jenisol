package examples.java.noreceiver.doublecomparison;

public class Incorrect0 {
  public double sum(double[] values) {
    double sum = 0.0;
    for (int i = 0; i < values.length - 1; i++) {
      sum += values[i];
    }
    return sum;
  }
}
