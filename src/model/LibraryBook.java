package model;

import datastructures.CustomQueue;
import java.util.Objects;

/**
 * Represents a book in the campus library.
 * This class tracks book details, availability, and manages a waiting list
 * for when the book is currently borrowed.
 */
public class LibraryBook {
    /** Unique identifier for the book */
    private final String id;
    /** Title of the book */
    private final String title;
    /** Author of the book */
    private final String author;
    /** International Standard Book Number */
    private final String isbn;
    /** Current availability status of the book */
    private boolean isAvailable;
    /** The student number of the student who currently has the book, null if available */
    private String borrowedByStudentId;
    /** FIFO queue of student IDs waiting for this book to be returned */
    private final CustomQueue<String> waitingListStudentIds;

    /**
     * Constructor for creating a new library book.
     * Initially available with an empty waiting list.
     *
     * @param id Unique book ID
     * @param title Book title
     * @param author Book author
     * @param isbn Book ISBN
     */
    public LibraryBook(String id, String title, String author, String isbn) {
        this(id, title, author, isbn, true, null, new CustomQueue<>());
    }

    /**
     * Full constructor for persistence and detailed initialization.
     *
     * @param id Unique book ID
     * @param title Book title
     * @param author Book author
     * @param isbn Book ISBN
     * @param isAvailable Current availability
     * @param borrowedByStudentId Current borrower
     * @param waitingListStudentIds Queue of students waiting
     */
    public LibraryBook(String id, String title, String author, String isbn, 
                       boolean isAvailable, String borrowedByStudentId, 
                       CustomQueue<String> waitingListStudentIds) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isAvailable = isAvailable;
        this.borrowedByStudentId = borrowedByStudentId;
        this.waitingListStudentIds = waitingListStudentIds != null ? waitingListStudentIds : new CustomQueue<>();
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public boolean isAvailable() { return isAvailable; }
    public String getBorrowedByStudentId() { return borrowedByStudentId; }
    public CustomQueue<String> getWaitingListStudentIds() { return waitingListStudentIds; }

    /**
     * Updates the availability and borrower of the book.
     * @param available true if returned, false if borrowed
     * @param studentId the student number of the borrower, or null if returned
     */
    public void setBorrowed(boolean available, String studentId) {
        this.isAvailable = available;
        this.borrowedByStudentId = studentId;
    }

    /**
     * Adds a student to the waiting list for this book.
     * @param studentId Student number to add
     */
    public void addToWaitingList(String studentId) {
        waitingListStudentIds.enqueue(studentId);
    }

    /**
     * Removes and returns the next student from the waiting list.
     * @return Next student number in line, or null if empty
     */
    public String popWaitingList() {
        if (waitingListStudentIds.isEmpty()) return null;
        return waitingListStudentIds.dequeue();
    }

    @Override
    public String toString() {
        String status = isAvailable ? "Available" : "Borrowed by " + borrowedByStudentId;
        if (!isAvailable && !waitingListStudentIds.isEmpty()) {
            status += " (Waitlist: " + waitingListStudentIds.size() + ")";
        }
        return String.format("[%s] %s by %s - %s", id, title, author, status);
    }
}
