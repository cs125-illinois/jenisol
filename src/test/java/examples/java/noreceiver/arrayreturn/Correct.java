package examples.java.noreceiver.arrayreturn;

public class Correct {
  int[] biggerTwo(int[] first, int[] second) {
    int aSum = first[0] + first[1];
    int bSum = second[0] + second[1];
    if (bSum > aSum) {
      return second;
    } else {
      return first;
    }
  }
}
