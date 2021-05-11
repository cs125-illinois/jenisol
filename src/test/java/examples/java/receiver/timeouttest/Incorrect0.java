package examples.java.receiver.timeouttest;

public class Incorrect0 {
  private final int first;
  private final int second;
  private final int third;
  private final int fourth;

  public Incorrect0(int setFirst, int setSecond, int setThird, int setFourth) {
    first = setFirst;
    second = setSecond;
    third = setThird;
    fourth = setFourth;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Incorrect0 correct = (Incorrect0) o;
    return first == correct.first
        && second == correct.second
        && third == correct.third
        && fourth == correct.fourth;
  }
}
