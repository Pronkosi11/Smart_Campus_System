package service;

import datastructures.CustomArrayList;
import datastructures.CustomHashMap;
import datastructures.CustomQueue;
import model.LibraryBook;
import model.Student;
import persistence.DataPersistence;
import ui.BoxUI;

/**
 * LibraryService - Library Module Business Logic
 *
 * This class is the "brain" of the library module. The UI (ConsoleUI)
 * never touches the data directly — it asks this service to do things
 * like "list all books" or "let student X borrow book B001".
 *
 * Responsibilities:
 *   - Keep the in-memory catalogue of {@link LibraryBook} objects.
 *   - Provide ADMIN actions: add a book, remove a book, change copy
 *     count, view all books and waiting lists.
 *   - Provide STUDENT actions: view available books, borrow a book,
 *     return a borrowed book, join a waiting list when nothing is free.
 *   - Save changes to {@code data/books.json} after every action so
 *     the data survives a restart.
 *
 * Why these data structures?
 * --------------------------
 *  - {@link CustomHashMap} stores books keyed by their bookId. Looking
 *    up "B001" is then an O(1) operation.
 *  - {@link CustomQueue} (inside each LibraryBook) holds the waiting
 *    list. Queues are perfect for "first come, first served".
 *  - {@link CustomArrayList} is used to return read-only snapshots of
 *    all books to the UI for printing.
 *
 * Pattern note: Singleton. There is only ONE LibraryService instance
 * in the whole program, so every part of the app sees the same data.
 */
public class LibraryService {

    // ---- Singleton plumbing ----
    private static LibraryService instance;

    // Primary storage: bookId -> LibraryBook. O(1) lookups.
    private CustomHashMap<String, LibraryBook> books;

    /**
     * Private constructor — outside code must call {@link #getInstance()}.
     */
    private LibraryService() {
        this.books = new CustomHashMap<>();
    }

    /**
     * Returns the single shared LibraryService instance, creating it
     * lazily the first time it is requested.
     */
    public static LibraryService getInstance() {
        if (instance == null) {
            instance = new LibraryService();
        }
        return instance;
    }

    // ============================================================
    //  PERSISTENCE INTEGRATION
    // ============================================================

    /**
     * Replaces the entire book catalogue. Used by the persistence layer
     * after loading {@code data/books.json} on startup.
     */
    public void setBooks(CustomHashMap<String, LibraryBook> loadedBooks) {
        this.books = (loadedBooks != null) ? loadedBooks : new CustomHashMap<>();
    }

    /**
     * Returns the internal map so the persistence layer can serialise it
     * back to JSON when the program shuts down.
     */
    public CustomHashMap<String, LibraryBook> getBooksMap() {
        return books;
    }

    // ============================================================
    //  CORE ADMIN AND STUDENT OPERATIONS
    // ============================================================

    /**
     * Adds a brand-new book to the catalogue. If a book with the same
     * id already exists it is overwritten — the admin should be careful.
     */
    public void addBook(LibraryBook book) {
        books.put(book.getBookId(), book);
        DataPersistence.saveBooks(books);
    }

    /**
     * Removes a book from the catalogue. Returns true if the book was
     * found and removed, false if there was no such id.
     */
    public boolean removeBook(String bookId) {
        LibraryBook removed = books.remove(bookId);
        if (removed != null) {
            DataPersistence.saveBooks(books);
            return true;
        }
        return false;
    }

    /**
     * Returns a flat list of every book in the catalogue. Useful for
     * printing the complete library inventory.
     */
    public CustomArrayList<LibraryBook> getAllBooks() {
        return books.values();
    }

    /**
     * Returns only the books that have at least one copy currently free.
     */
    public CustomArrayList<LibraryBook> getAvailableBooks() {
        CustomArrayList<LibraryBook> available = new CustomArrayList<>();
        CustomArrayList<LibraryBook> all = books.values();
        for (int i = 0; i < all.size(); i++) {
            LibraryBook b = all.get(i);
            if (b.hasAvailableCopy()) {
                available.add(b);
            }
        }
        return available;
    }

    /**
     * Lookup helper — returns null when the id is unknown.
     */
    public LibraryBook getBook(String bookId) {
        return books.get(bookId);
    }

    /**
     * Tries to borrow a book for a student. The result tells the UI
     * exactly what happened so it can show a friendly message.
     *
     * BorrowResult enum values:
     *   - SUCCESS         : the student now has the book
     *   - QUEUED          : no copies available, student joined the queue
     *   - ALREADY_BORROWED: the student already has this book
     *   - ALREADY_QUEUED  : the student is already on the waiting list
     *   - BOOK_NOT_FOUND  : invalid book id
     */
    public enum BorrowResult { SUCCESS, QUEUED, ALREADY_BORROWED, ALREADY_QUEUED, BOOK_NOT_FOUND }

    public BorrowResult borrowBook(String studentNumber, String bookId) {
        LibraryBook book = books.get(bookId);
        if (book == null) {
            return BorrowResult.BOOK_NOT_FOUND;
        }

        Student student = StudentService.getInstance().getStudent(studentNumber);
        if (student == null) {
            return BorrowResult.BOOK_NOT_FOUND; // unknown student
        }

        // Refuse duplicate borrowing of the same title.
        if (student.getBorrowedBooks().contains(bookId)) {
            return BorrowResult.ALREADY_BORROWED;
        }

        if (book.hasAvailableCopy()) {
            // Take a copy off the shelf and remember who has it.
            book.borrowOneCopy();
            student.borrowBook(bookId);
            StudentService.getInstance().updateStudent(student);
            DataPersistence.saveBooks(books);
            return BorrowResult.SUCCESS;
        }

        // No copies available — offer the waiting list.
        if (queueContains(book.getWaitingList(), studentNumber)) {
            return BorrowResult.ALREADY_QUEUED;
        }
        book.getWaitingList().enqueue(studentNumber);
        DataPersistence.saveBooks(books);
        return BorrowResult.QUEUED;
    }

    /**
     * Returns a borrowed book. If somebody is waiting in the queue, the
     * book is immediately handed to them and the result reports that.
     *
     * ReturnResult enum values:
     *   - SUCCESS_AND_FREE   : book returned, no waiting list
     *   - SUCCESS_HANDED_OFF : book returned and instantly given to next student
     *   - NOT_BORROWED       : the student did not have this book
     *   - BOOK_NOT_FOUND     : invalid book id
     */
    public enum ReturnResult { SUCCESS_AND_FREE, SUCCESS_HANDED_OFF, NOT_BORROWED, BOOK_NOT_FOUND }

    public ReturnResult returnBook(String studentNumber, String bookId) {
        LibraryBook book = books.get(bookId);
        if (book == null) {
            return ReturnResult.BOOK_NOT_FOUND;
        }

        Student student = StudentService.getInstance().getStudent(studentNumber);
        if (student == null || !student.getBorrowedBooks().contains(bookId)) {
            return ReturnResult.NOT_BORROWED;
        }

        // Remove from this student's borrowed list.
        student.returnBook(bookId);
        StudentService.getInstance().updateStudent(student);

        // If somebody is waiting, hand the book to them right away.
        if (!book.getWaitingList().isEmpty()) {
            String nextStudentNumber = book.getWaitingList().dequeue();
            Student nextStudent = StudentService.getInstance().getStudent(nextStudentNumber);
            if (nextStudent != null) {
                // Note: we do NOT increase availableCopies because the
                // book is going straight back out the door.
                nextStudent.borrowBook(bookId);
                StudentService.getInstance().updateStudent(nextStudent);
                DataPersistence.saveBooks(books);
                return ReturnResult.SUCCESS_HANDED_OFF;
            }
        }

        // Otherwise the copy goes back on the shelf.
        book.returnOneCopy();
        DataPersistence.saveBooks(books);
        return ReturnResult.SUCCESS_AND_FREE;
    }

    // ============================================================
    //  ADMIN MENU (called from ConsoleUI)
    // ============================================================

    public void showAdminLibraryMenu(BoxUI box) {
        int choice;
        do {
            box.printMenu("MANAGE LIBRARY", new String[]{
                    "1. List All Books",
                    "2. Add New Book",
                    "3. Remove Book",
                    "4. View Waiting List for a Book",
                    "5. Back"
            });
            choice = box.readInt("Choose option: ", 1, 5);
            switch (choice) {
                case 1: listAllBooks(box); break;
                case 2: addBookFlow(box); break;
                case 3: removeBookFlow(box); break;
                case 4: viewWaitingListFlow(box); break;
                case 5: break;
                default: box.error("Invalid option.");
            }
        } while (choice != 5);
    }

    // ============================================================
    //  STUDENT MENU (called from ConsoleUI)
    // ============================================================

    public void showStudentLibraryMenu(BoxUI box, Student student) {
        int choice;
        do {
            box.printMenu("LIBRARY SERVICES", new String[]{
                    "1. Browse Available Books",
                    "2. Borrow a Book",
                    "3. Return a Book",
                    "4. View My Borrowed Books",
                    "5. Back"
            });
            choice = box.readInt("Choose option: ", 1, 5);
            switch (choice) {
                case 1: listAvailableBooks(box); break;
                case 2: borrowFlow(box, student); break;
                case 3: returnFlow(box, student); break;
                case 4: listMyBorrowed(box, student); break;
                case 5: break;
                default: box.error("Invalid option.");
            }
        } while (choice != 5);
    }

    // ============================================================
    //  UI FLOW HELPERS (private — beginners: think of these as the
    //  "screens" that prompt the user and call the public methods)
    // ============================================================

    private void listAllBooks(BoxUI box) {
        CustomArrayList<LibraryBook> all = getAllBooks();
        if (all.isEmpty()) {
            box.info("No books in the library yet.");
            return;
        }
        box.printSection("LIBRARY CATALOGUE");
        for (int i = 0; i < all.size(); i++) {
            box.line((i + 1) + ". " + all.get(i));
        }
        box.endSection();
    }

    private void listAvailableBooks(BoxUI box) {
        CustomArrayList<LibraryBook> avail = getAvailableBooks();
        if (avail.isEmpty()) {
            box.info("No books are currently available. Try the waiting list option when borrowing.");
            return;
        }
        box.printSection("AVAILABLE BOOKS");
        for (int i = 0; i < avail.size(); i++) {
            box.line((i + 1) + ". " + avail.get(i));
        }
        box.endSection();
    }

    private void addBookFlow(BoxUI box) {
        String id = box.prompt("Book ID (e.g. B014): ");
        if (id.isEmpty()) { box.error("Book ID is required."); return; }
        if (books.get(id) != null) { box.error("A book with that ID already exists."); return; }

        String title = box.prompt("Title: ");
        if (title.isEmpty()) { box.error("Title is required."); return; }
        String author = box.prompt("Author: ");
        if (author.isEmpty()) { box.error("Author is required."); return; }
        String isbn = box.prompt("ISBN: ");
        if (isbn.isEmpty()) { isbn = "N/A"; }
        int copies = box.readInt("Total copies (1-100): ", 1, 100);

        addBook(new LibraryBook(id, title, author, isbn, copies));
        box.success("Book added.");
    }

    private void removeBookFlow(BoxUI box) {
        String id = box.prompt("Book ID to remove: ");
        if (removeBook(id)) {
            box.success("Book removed.");
        } else {
            box.error("No book found with that ID.");
        }
    }

    private void viewWaitingListFlow(BoxUI box) {
        String id = box.prompt("Book ID: ");
        LibraryBook book = books.get(id);
        if (book == null) { box.error("No book found with that ID."); return; }

        // Snapshot the waiting list without disturbing it.
        CustomArrayList<String> waiting = snapshotQueue(book.getWaitingList());
        if (waiting.isEmpty()) {
            box.info("No students are waiting for this book.");
            return;
        }
        box.printSection("WAITING LIST - " + book.getTitle());
        for (int i = 0; i < waiting.size(); i++) {
            box.line((i + 1) + ". " + waiting.get(i));
        }
        box.endSection();
    }

    private void borrowFlow(BoxUI box, Student student) {
        String id = box.prompt("Book ID to borrow: ");
        BorrowResult result = borrowBook(student.getStudentNumber(), id);
        switch (result) {
            case SUCCESS:          box.success("Borrowed. Please return it on time."); break;
            case QUEUED:           box.info("All copies are out. You have been added to the waiting list."); break;
            case ALREADY_BORROWED: box.error("You already have this book."); break;
            case ALREADY_QUEUED:   box.error("You are already on the waiting list for this book."); break;
            case BOOK_NOT_FOUND:   box.error("No book found with that ID."); break;
        }
    }

    private void returnFlow(BoxUI box, Student student) {
        String id = box.prompt("Book ID to return: ");
        ReturnResult result = returnBook(student.getStudentNumber(), id);
        switch (result) {
            case SUCCESS_AND_FREE:   box.success("Book returned. Thank you!"); break;
            case SUCCESS_HANDED_OFF: box.success("Book returned. It has been handed to the next student in the waiting list."); break;
            case NOT_BORROWED:       box.error("You do not have that book."); break;
            case BOOK_NOT_FOUND:     box.error("No book found with that ID."); break;
        }
    }

    private void listMyBorrowed(BoxUI box, Student student) {
        CustomArrayList<String> mine = student.getBorrowedBooks();
        if (mine.isEmpty()) {
            box.info("You have no borrowed books.");
            return;
        }
        box.printSection("MY BORROWED BOOKS");
        for (int i = 0; i < mine.size(); i++) {
            String id = mine.get(i);
            LibraryBook b = books.get(id);
            box.line((i + 1) + ". " + (b != null ? b.toString() : id));
        }
        box.endSection();
    }

    // ============================================================
    //  PRIVATE UTILITIES
    // ============================================================

    /**
     * Copies a queue's contents into a list WITHOUT permanently
     * disturbing the queue. We dequeue everything into a temp queue,
     * then put it all back so the original order is preserved.
     */
    private CustomArrayList<String> snapshotQueue(CustomQueue<String> queue) {
        CustomArrayList<String> out = new CustomArrayList<>();
        CustomQueue<String> temp = new CustomQueue<>();
        while (!queue.isEmpty()) {
            String x = queue.dequeue();
            out.add(x);
            temp.enqueue(x);
        }
        while (!temp.isEmpty()) {
            queue.enqueue(temp.dequeue());
        }
        return out;
    }

    /**
     * Returns true if the given student number is somewhere in the queue.
     * Uses snapshotQueue so the queue is not modified.
     */
    private boolean queueContains(CustomQueue<String> queue, String studentNumber) {
        CustomArrayList<String> snapshot = snapshotQueue(queue);
        for (int i = 0; i < snapshot.size(); i++) {
            if (studentNumber.equalsIgnoreCase(snapshot.get(i))) {
                return true;
            }
        }
        return false;
    }
}
