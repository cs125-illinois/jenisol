package examples.java.submissiondesign.noconverter;

public class BadSolution1 {
  private final int v;

  private BadSolution1(int value) {
    v = value;
  }

  public static BadSolution1 create(int value) {
    return new BadSolution1(value);
  }
}
