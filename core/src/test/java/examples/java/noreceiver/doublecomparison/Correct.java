package examples.java.noreceiver.doublecomparison;

public class Correct {
  public double sum(double[] values) {
    double sum = 0.0;
    for (int i = 0; i < values.length; i++) {
      sum += values[i];
    }
    return sum;
  }
}
