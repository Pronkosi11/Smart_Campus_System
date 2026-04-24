package service;

import datastructures.CustomArrayList;
import datastructures.CustomHashMap;
import datastructures.CustomQueue;
import model.LibraryBook;
import model.Student;
import persistence.DataPersistence;
import ui.BoxUI;

/**
 * Service class for library operations.
 * Handles book management for admins and borrowing/returning for students.
 */
public class LibraryService {
    private static LibraryService instance;
    /** In-memory storage for library books, keyed by book ID */
    private CustomHashMap<String, LibraryBook> books = new CustomHashMap<>();

    /**
     * Private constructor for Singleton pattern.
     */
    private LibraryService() {
    }

    /**
     * Returns the singleton instance of LibraryService.
     */
    public static LibraryService getInstance() {
        if (instance == null) {
            instance = new LibraryService();
        }
        return instance;
    }

    /**
     * Sets the book data loaded from persistence.
     * @param loadedBooks HashMap of books
     */
    public void setBooks(CustomHashMap<String, LibraryBook> loadedBooks) {
        this.books = loadedBooks;
    }

    /**
     * Returns the internal map of books for persistence saving.
     */
    public CustomHashMap<String, LibraryBook> getBooksMap() {
        return books;
    }

    /**
     * Displays the administrative library menu.
     * @param box UI utility for rendering
     */
    public void showAdminLibraryMenu(BoxUI box) {
        int choice;
        do {
            box.printMenu("LIBRARY MANAGEMENT (ADMIN)", new String[]{
                    "1. Add New Book",
                    "2. Remove Book",
                    "3. List All Books",
                    "4. View Waiting List",
                    "5. Back to Main Menu"
            });
            choice = box.readInt("Choose option: ", 1, 5);
            switch (choice) {
                case 1: addBookFlow(box); break;
                case 2: removeBookFlow(box); break;
                case 3: listAllBooks(box); break;
                case 4: viewWaitingListFlow(box); break;
            }
        } while (choice != 5);
    }

    /**
     * Displays the student library portal.
     * @param box UI utility
     * @param student Currently logged-in student
     */
    public void showStudentLibraryMenu(BoxUI box, Student student) {
        int choice;
        do {
            box.printMenu("LIBRARY SERVICES", new String[]{
                    "1. Browse All Books",
                    "2. Search Book by ID",
                    "3. Borrow a Book",
                    "4. Return a Book",
                    "5. View My Borrowed Books",
                    "6. Back to Portal"
            });
            choice = box.readInt("Choose option: ", 1, 6);
            switch (choice) {
                case 1: listAllBooks(box); break;
                case 2: searchBookFlow(box); break;
                case 3: borrowBookFlow(box, student); break;
                case 4: returnBookFlow(box, student); break;
                case 5: listStudentBooks(box, student); break;
            }
        } while (choice != 6);
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
