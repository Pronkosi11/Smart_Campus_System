package service;

import datastructures.CustomArrayList;
import datastructures.CustomHashMap;
import datastructures.CustomQueue;
import model.LibraryBook;
import model.Student;
import persistence.DataPersistence;
import ui.BoxUI;

/**
 * LibraryService - Library Management Service
 * 
 * This service class handles all library-related operations in the Smart Campus System.
 * It provides comprehensive library management functionality including:
 * 
 * Core Operations:
 * - Book creation, retrieval, update, and deletion (CRUD operations)
 * - Book borrowing and returning for students
 * - Waiting list management for popular books
 * - Book availability tracking
 * 
 * UI Integration:
 * - Admin interface for book management
 * - Student library interface for borrowing/returning
 * - Clean BoxUI-based console interface
 * 
 * Data Management:
 * - Uses CustomHashMap for efficient book storage (O(1) lookup by book ID)
 * - CustomQueue for managing waiting lists (FIFO order)
 * - Automatic persistence through DataPersistence layer
 * - Integration with StudentService for tracking borrowed books
 * 
 * Key Concepts Demonstrated:
 * - Singleton design pattern (single instance across application)
 * - Service layer architecture (separates business logic from UI)
 * - Queue data structure for waiting lists
 * - Book availability management
 * - Student borrowing history tracking
 * 
 * This class follows the Singleton pattern to ensure centralized library data management
 * across the entire application, preventing data inconsistencies.
 */
public class LibraryService {
    
    /** 
     * Singleton instance to ensure centralized library management.
     * This ensures all parts of the application work with the same library data.
     */
    private static LibraryService instance;
    
    /** 
     * In-memory storage for library books, keyed by book ID for fast lookup.
     * Using CustomHashMap provides O(1) average time complexity for book retrieval.
     */
    private CustomHashMap<String, LibraryBook> books = new CustomHashMap<>();

    /**
     * Private constructor to enforce singleton pattern.
     * 
     * This constructor is private to prevent direct instantiation.
     * Use getInstance() to get the singleton instance.
     */
    private LibraryService() {
    }

    /**
     * Gets the singleton instance of LibraryService.
     * 
     * This method implements the lazy initialization singleton pattern:
     * - If no instance exists, create one
     * - Return the existing instance
     * 
     * This ensures only one LibraryService exists throughout the application.
     * 
     * @return The singleton LibraryService instance
     */
    public static LibraryService getInstance() {
        if (instance == null) {
            instance = new LibraryService();
        }
        return instance;
    }

    // ========== DATA MANAGEMENT METHODS ==========
    
    /**
     * Sets the book data loaded from persistence storage.
     * 
     * This method is used by the persistence layer to restore book data
     * when the application starts up. It replaces the entire book collection.
     * 
     * @param loadedBooks HashMap of books loaded from storage
     */
    public void setBooks(CustomHashMap<String, LibraryBook> loadedBooks) {
        this.books = loadedBooks;
    }

    /**
     * Returns the internal map of books for persistence saving.
     * 
     * This method provides access to the underlying data structure for
     * persistence operations and other system components.
     * 
     * @return The internal HashMap containing all books
     */
    public CustomHashMap<String, LibraryBook> getBooksMap() {
        return books;
    }

    // ========== USER INTERFACE METHODS ==========
    
    /**
     * Displays the administrative library menu.
     * 
     * This method provides the main interface for administrators to manage the library:
     * - Add new books to the collection
     * - Remove books from the collection
     * - List all books in the library
     * - View waiting lists for popular books
     * - Return to admin dashboard
     * 
     * The menu uses a do-while loop to persist until the user chooses to exit,
     * providing a continuous workflow for library management tasks.
     * 
     * @param box The BoxUI instance for rendering the interface
     */
    public void showAdminLibraryMenu(BoxUI box) {
        int choice;
        do {
            // Display the library management menu options
            box.printMenu("LIBRARY MANAGEMENT (ADMIN)", new String[]{
                    "1. Add New Book",
                    "2. Remove Book",
                    "3. List All Books",
                    "4. View Waiting List",
                    "5. Back to Main Menu"
            });
            
            // Get user's menu choice with validation
            choice = box.readInt("Choose option: ", 1, 5);
            
            // Execute the selected action
            switch (choice) {
                case 1: 
                    addBookFlow(box);        // Add a new book to library
                    break;
                case 2: 
                    removeBookFlow(box);     // Remove an existing book
                    break;
                case 3: 
                    listAllBooks(box);       // Display all books
                    break;
                case 4: 
                    viewWaitingListFlow(box); // View waiting lists
                    break;
                // Case 5 (Back) handled by loop condition
            }
        } while (choice != 5);  // Continue until user chooses "Back"
    }

    /**
     * Displays the student library portal.
     * 
     * This method provides a comprehensive library interface for students:
     * - Browse all available books
     * - Search for specific books by ID
     * - Borrow available books
     * - Return borrowed books
     * - View currently borrowed books
     * - Return to student portal
     * 
     * The interface uses a do-while loop to allow multiple library actions
     * before returning to the student dashboard.
     * 
     * @param box The BoxUI instance for rendering the interface
     * @param student The currently logged-in student
     */
    public void showStudentLibraryMenu(BoxUI box, Student student) {
        int choice;
        do {
            // Display the student library menu options
            box.printMenu("LIBRARY SERVICES", new String[]{
                    "1. Browse All Books",
                    "2. Search Book by ID",
                    "3. Borrow a Book",
                    "4. Return a Book",
                    "5. View My Borrowed Books",
                    "6. Back to Portal"
            });
            
            // Get user's menu choice with validation
            choice = box.readInt("Choose option: ", 1, 6);
            
            // Execute the selected action
            switch (choice) {
                case 1: 
                    listAllBooks(box);           // Browse all books
                    break;
                case 2: 
                    searchBookFlow(box);         // Search for specific book
                    break;
                case 3: 
                    borrowBookFlow(box, student); // Borrow a book
                    break;
                case 4: 
                    returnBookFlow(box, student); // Return a book
                    break;
                case 5: 
                    listStudentBooks(box, student); // View borrowed books
                    break;
                // Case 6 (Back) handled by loop condition
            }
        } while (choice != 6);  // Continue until user chooses "Back"
    }

    // --- Admin Operations ---

    private void addBookFlow(BoxUI box) {
        String id = box.prompt("Enter Book ID (e.g., BK-101): ");
        if (books.containsKey(id)) {
            box.error("Book with ID " + id + " already exists.");
            return;
        }
        String title = box.prompt("Enter Title: ");
        String author = box.prompt("Enter Author: ");
        String isbn = box.prompt("Enter ISBN: ");

        LibraryBook book = new LibraryBook(id, title, author, isbn);
        books.put(id, book);
        DataPersistence.saveBooks(books);
        box.success("Book added successfully.");
    }

    private void removeBookFlow(BoxUI box) {
        String id = box.prompt("Enter Book ID to remove: ");
        if (books.remove(id) != null) {
            DataPersistence.saveBooks(books);
            box.success("Book removed.");
        } else {
            box.error("Book not found.");
        }
    }

    private void viewWaitingListFlow(BoxUI box) {
        String id = box.prompt("Enter Book ID: ");
        LibraryBook b = books.get(id);
        if (b == null) {
            box.error("Book not found.");
            return;
        }
        CustomQueue<String> q = b.getWaitingListStudentIds();
        if (q.isEmpty()) {
            box.info("No students in waiting list for this book.");
        } else {
            box.info("Waiting list for " + b.getTitle() + ": " + q.size() + " students.");
        }
    }

    // --- Student Operations ---

    private void borrowBookFlow(BoxUI box, Student student) {
        String id = box.prompt("Enter Book ID to borrow: ");
        LibraryBook b = books.get(id);
        if (b == null) {
            box.error("Book not found.");
            return;
        }

        if (b.isAvailable()) {
            b.setBorrowed(false, student.getStudentNumber());
            student.borrowBook(id);
            DataPersistence.saveBooks(books);
            DataPersistence.saveStudents(StudentService.getInstance().getStudentsMap());
            box.success("You have successfully borrowed: " + b.getTitle());
        } else {
            if (student.getStudentNumber().equals(b.getBorrowedByStudentId())) {
                box.error("You already have this book.");
                return;
            }
            box.info("Book is currently borrowed by another student.");
            String join = box.prompt("Would you like to join the waiting list? (y/n): ");
            if ("y".equalsIgnoreCase(join)) {
                b.addToWaitingList(student.getStudentNumber());
                DataPersistence.saveBooks(books);
                box.success("Added to waiting list.");
            }
        }
    }

    private void returnBookFlow(BoxUI box, Student student) {
        String id = box.prompt("Enter Book ID to return: ");
        LibraryBook b = books.get(id);
        if (b == null) {
            box.error("Book not found in library records.");
            return;
        }

        if (student.getBorrowedBooks().contains(id)) {
            student.returnBook(id);
            String nextStudent = b.popWaitingList();
            if (nextStudent != null) {
                box.info("Book " + b.getTitle() + " immediately assigned to next student in waitlist: " + nextStudent);
                b.setBorrowed(false, nextStudent);
                Student nextS = StudentService.getInstance().getStudentByNumber(nextStudent);
                if (nextS != null) nextS.borrowBook(id);
            } else {
                b.setBorrowed(true, null);
            }
            DataPersistence.saveBooks(books);
            DataPersistence.saveStudents(StudentService.getInstance().getStudentsMap());
            box.success("Book returned successfully.");
        } else {
            box.error("You don't have this book checked out.");
        }
    }

    private void listStudentBooks(BoxUI box, Student student) {
        CustomArrayList<String> borrowed = student.getBorrowedBooks();
        if (borrowed.isEmpty()) {
            box.info("You have no borrowed books.");
            return;
        }
        String[] lines = new String[borrowed.size()];
        int count = 0;
        for (int i = 0; i < borrowed.size(); i++) {
            LibraryBook b = books.get(borrowed.get(i));
            if (b != null) {
                lines[count++] = b.toString();
            }
        }
        box.printSection("Your Borrowed Books", lines);
        for (int i = 0; i < count; i++) {
            box.line(lines[i]);
        }
        box.endSection();
    }

    // --- Common ---

    private void listAllBooks(BoxUI box) {
        CustomArrayList<LibraryBook> all = books.values();
        if (all.isEmpty()) {
            box.info("No books in the library.");
            return;
        }
        String[] lines = new String[all.size()];
        for (int i = 0; i < all.size(); i++) {
            lines[i] = all.get(i).toString();
        }
        box.printSection("Current Library Inventory", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    private void searchBookFlow(BoxUI box) {
        String id = box.prompt("Enter Book ID: ");
        LibraryBook b = books.get(id);
        if (b != null) {
            box.info("Book Found: " + b.toString());
        } else {
            box.error("Book not found.");
        }
    }
}
