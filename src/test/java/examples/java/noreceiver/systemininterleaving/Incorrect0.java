package examples.java.noreceiver.systemininterleaving;

import java.util.Scanner;

public class Incorrect0 {
  public static void echo() {
    Scanner scanner = new Scanner(System.in);
    String first = scanner.nextLine();
    String second = scanner.nextLine();
    System.out.println("First: " + first);
    System.out.println("Second: " + first);
  }
}
