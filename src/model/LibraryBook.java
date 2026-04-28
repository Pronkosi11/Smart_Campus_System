package model;

import datastructures.CustomQueue;

/**
 * LibraryBook - Library Book Entity Model
 *
 * Represents a single physical title in the campus library. Each title
 * can have multiple physical copies (e.g. three copies of "Clean Code"),
 * and the library tracks how many copies are currently on the shelf
 * versus how many are checked out.
 *
 * Why these data structures?
 * --------------------------
 *  - We use a {@link CustomQueue} for the waiting list because students
 *    waiting for a book should be served in the SAME order they joined
 *    the queue (First-In, First-Out). This is exactly what a queue does.
 *
 *  - Available copies are tracked as a simple integer counter — it is the
 *    cheapest way to know "is there a copy free right now?" without
 *    looping through the physical copies one by one.
 *
 * Beginner notes:
 *  - All fields are PRIVATE so outside code cannot accidentally break
 *    the rules (e.g. setting availableCopies above totalCopies).
 *  - Public methods are the only safe way to interact with this class.
 *  - The constructor sets sensible starting values, so a brand-new book
 *    immediately has a working empty waiting list.
 */
public class LibraryBook {

    // ========== BASIC BOOK INFORMATION ==========
    // The unique identifier we use to look this book up (e.g. "B001").
    private String bookId;
    // Human-readable title shown in menus (e.g. "Clean Code").
    private String title;
    // The author or authors of the book.
    private String author;
    // International Standard Book Number — useful for cataloguing.
    private String isbn;

    // ========== INVENTORY TRACKING ==========
    // How many physical copies the library OWNS in total (never changes
    // during normal borrow/return — only when admin adds/removes copies).
    private int totalCopies;
    // How many copies are currently on the shelf and available to borrow.
    // Must always satisfy: 0 <= availableCopies <= totalCopies.
    private int availableCopies;

    // ========== WAITING LIST (QUEUE) ==========
    // A FIFO queue of student numbers that asked for the book while no
    // copies were available. When a copy is returned, the student at the
    // FRONT of the queue is the next person to be served.
    private CustomQueue<String> waitingList;

    /**
     * Builds a brand-new book record.
     *
     * Both totalCopies and availableCopies start equal because all copies
     * are on the shelf when the library first registers the title.
     *
     * @param bookId      unique identifier (e.g. "B001") — must not be null
     * @param title       full book title shown to students
     * @param author      author name (single string, even if multiple authors)
     * @param isbn        ISBN code, used for cataloguing
     * @param totalCopies how many physical copies the library owns
     */
    public LibraryBook(String bookId, String title, String author, String isbn, int totalCopies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        // Defensive guard: copies cannot be negative.
        this.totalCopies = Math.max(0, totalCopies);
        // When created, every copy is on the shelf.
        this.availableCopies = this.totalCopies;
        // Start with an empty waiting list.
        this.waitingList = new CustomQueue<>();
    }

    // ========== GETTERS AND SETTERS ==========
    // Simple accessors. Setters do basic validation where it matters.

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getTotalCopies() { return totalCopies; }

    public int getAvailableCopies() { return availableCopies; }

    public CustomQueue<String> getWaitingList() { return waitingList; }

    /**
     * Used by the persistence layer to restore inventory after loading
     * the JSON file. Beginners: think of this as "trust the JSON file —
     * it already knows the correct numbers from the previous run".
     */
    public void restoreInventory(int totalCopies, int availableCopies) {
        this.totalCopies = Math.max(0, totalCopies);
        // Make sure availableCopies cannot exceed totalCopies.
        this.availableCopies = Math.max(0, Math.min(availableCopies, this.totalCopies));
    }

    /**
     * Used by the persistence layer to set the waiting list reference
     * after rebuilding it from the JSON file.
     */
    public void setWaitingList(CustomQueue<String> waitingList) {
        this.waitingList = (waitingList != null) ? waitingList : new CustomQueue<>();
    }

    // ========== INVENTORY HELPER METHODS ==========

    /**
     * Quick check: are there any copies available right now?
     */
    public boolean hasAvailableCopy() {
        return availableCopies > 0;
    }

    /**
     * Take one copy off the shelf when a student borrows it.
     * Returns true if a copy was successfully reserved, false if none
     * were available (caller should then offer the waiting list instead).
     */
    public boolean borrowOneCopy() {
        if (availableCopies <= 0) {
            return false;
        }
        availableCopies--;
        return true;
    }

    /**
     * Put one copy back on the shelf when a student returns a book.
     * We never go above totalCopies — that would be a corrupt state.
     */
    public void returnOneCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    /**
     * Concise display string used in menu listings, e.g.
     * "B001 - Clean Code by Robert Martin [3/3 available]".
     */
    @Override
    public String toString() {
        return String.format("%s - %s by %s [%d/%d available]",
                bookId, title, author, availableCopies, totalCopies);
    }
}
