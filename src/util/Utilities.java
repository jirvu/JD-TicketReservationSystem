package util;
import java.util.Scanner;

public class Utilities {
    private static final Scanner scanner;

    public static int getUserInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    public static String getUserText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    static {
        scanner = new Scanner(System.in);
    }
}
