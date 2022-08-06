package examples.java.noreceiver.systemininterleaving;

import java.util.Scanner;

public class Incorrect1 {
  public static void echo() {
    Scanner scanner = new Scanner(System.in);
    String first = scanner.nextLine();
    System.out.println("Hello!");
    System.out.println("First: " + first);
    String second = scanner.nextLine();
    System.out.println("Second: " + second);
  }
}
