package model;

public class Book {

    private String title;
    private boolean available = true;

    public Book(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAvailable() {
        return available;
    }

    public void borrow() {
        available = false;
    }

    public void giveBack() {
        available = true;
    }

    public String toString() {
        return title + " | " + (available ? "Available" : "Borrowed");
    }
}