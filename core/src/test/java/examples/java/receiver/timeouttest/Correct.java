package examples.java.receiver.timeouttest;

public class Correct {
  private final int first;
  private final int second;
  private final int third;
  private final int fourth;

  public Correct(int setFirst, int setSecond, int setThird, int setFourth) {
    assert setFourth > 0;
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
    Correct correct = (Correct) o;
    return first == correct.first
        && second == correct.second
        && third == correct.third
        && fourth == correct.fourth;
  }
}
