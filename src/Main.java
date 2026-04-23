
import ui.ConsoleUI;
import persistence.DataPersistence;

/**
 * Smart Campus Management System - Main Entry Point
 * 
 * This is a comprehensive campus management system that provides:
 * - Student registration and profile management
 * - Course enrollment and management
 * - Help desk ticketing system
 * - Administrative dashboard
 * - Clean console-based user interface with BoxUI
 * 
 * The system uses custom data structures (ArrayList, HashMap, LinkedList, Queue, Stack)
 * and JSON-based persistence for data storage.
 * 
 * @author Smart Campus Development Team
 * @version 1.0
 */
public class Main {

    /**
     * Application entry point that orchestrates the startup and shutdown sequence.
     * 
     * The main method follows this sequence:
     * 1. Load all persistent data from JSON files (students, courses, tickets, etc.)
     * 2. Initialize and start the console-based user interface
     * 3. Handle user interactions through the UI until exit
     * 4. Save all data back to JSON files before termination
     * 5. Display friendly goodbye message
     * 
     * @param args Command line arguments (not used in this application)
     */
    public static void main(String[] args) {
        // Load all persistent data from JSON files into memory
        // This includes students, courses, tickets, and other system data
        DataPersistence.loadAllData();

        // Initialize the main console user interface and start the application
        // ConsoleUI handles login, menus, and routing to different modules
        ConsoleUI consoleUI = new ConsoleUI();
        consoleUI.start();

        // Persist all in-memory data back to JSON files before application exit
        // This ensures no data is lost between sessions
        DataPersistence.saveAllData();

        // Display friendly exit messages to the user
        System.out.println("\nThank you for using Smart Campus Management System!");
        System.out.println("Goodbye!");
    }
}
