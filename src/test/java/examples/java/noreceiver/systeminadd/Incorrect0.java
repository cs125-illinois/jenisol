package examples.java.noreceiver.systeminadd;

import java.util.Scanner;

public class Incorrect0 {
  public static void main() {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter a number: ");
    int first = scanner.nextInt();
    System.out.println("Enter another number: ");
    int second = scanner.nextInt();
    System.out.println("The sum is " + (first + second));
  }
}
