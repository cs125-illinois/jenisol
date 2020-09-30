package examples.java.receiver.doublecomparison;

public class Correct {
  private double radius;

  public Correct(double setRadius) {
    radius = setRadius;
  }

  public double area() {
    return Math.PI * radius * radius;
  }
}
