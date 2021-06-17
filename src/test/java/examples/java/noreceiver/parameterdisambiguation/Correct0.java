package examples.java.noreceiver.parameterdisambiguation;

import edu.illinois.cs.cs125.jenisol.core.NotNull;

@SuppressWarnings("StringConcatenationInLoop")
public class Correct0 {
  public static int length(String input) {
    if (!(input.equals("a".repeat(input.length())))) {
      return 0;
    }
    return input.length();
  }

  public static String reversed(@NotNull String input) {
    if (input.equals("a".repeat(input.length()))) {
      return "";
    }
    String result = "";
    for (int i = 0; i < input.length(); i++) {
      result += input.charAt(input.length() - i - 1);
    }
    return result;
  }
}
