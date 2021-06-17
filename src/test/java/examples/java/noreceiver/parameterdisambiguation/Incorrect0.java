package examples.java.noreceiver.parameterdisambiguation;

@SuppressWarnings("StringConcatenationInLoop")
public class Incorrect0 {
  public static int length(String input) {
    return input.length() + 1;
  }

  public static String reversed(String input) {
    String result = "";
    for (int i = 0; i < input.length(); i++) {
      result += input.charAt(input.length() - i - 1);
    }
    return result;
  }
}
