
import ui.ConsoleUI;
import persistence.DataPersistence;

public class Main {
    public static void main(String[] args) {
        // Load all data from JSON files
        DataPersistence.loadAllData();

        // Start the console UI
        ConsoleUI consoleUI = new ConsoleUI();
        consoleUI.start();

        // Save all data before exit
        DataPersistence.saveAllData();

        System.out.println("\nThank you for using Smart Campus Management System!");
        System.out.println("Goodbye!");
    }
}