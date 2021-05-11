package examples.java.receiver.doublecomparison;

public class Correct0 {
  private double radius;

  public Correct0(double setRadius) {
    radius = setRadius;
  }

  public double area() {
    return radius * radius * Math.PI;
  }
}
