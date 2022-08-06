package examples.java.noreceiver.usessystemin;

import java.util.Scanner;

public class Incorrect0 {
  public static void echo() {
    Scanner scanner = new Scanner(System.in);
    System.out.println(scanner.nextLine() + "test");
  }
}
