package ui;
import java.util.*;

public class MainMenu {
    private Scanner input = new Scanner(System.in);

    public void start() {
        int choice;

        do {
            System.out.println("\n===== SMART CAMPUS SYSTEM =====");
            System.out.println("1. Register Student");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");

            choice = input.nextInt();
            input.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("You selected Register Student");
                    break;
                case 2:
                    System.out.println("You selected login");
                    break;
                case 3:
                    System.out.println("You exited the program");
                    break;
                default :
                    System.out.println("Invalid option. Please try again.");
                    break;
            }

        } while (choice != 3);
    }
}
