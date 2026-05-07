package datastructures;

import java.util.NoSuchElementException;

/**
 * CustomQueue - First-In-First-Out (FIFO) Queue Implementation
 * 
 * A queue is like a line of people waiting at a store.
 * The first person who arrives is the first person to be served (First In, First Out).
 * New people always join at the back (end) of the line.
 * 
 * Real-life examples:
 * - Waiting in line at a cafeteria
 * - Printing documents in order
 * - Help desk tickets (first ticket submitted gets handled first)
 * 
 * This queue uses a linked list structure, so it can grow as needed.
 */
public class CustomQueue<T> {
    
    /** The front of the line (first person to be served) */
    private Node<T> front;
    
    /** The back of the line (where new people join) */
    private Node<T> rear;
    
    /** How many people (items) are in the queue */
    private int size;

    /**
     * A Node is a single person in the line.
     * Each person knows who is behind them (next).
     */
    private static class Node<T> {
        /** The data/value this person/node is holding */
        T data;
        
        /** The next person in line (the person behind them) */
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    /** Creates a new empty queue (an empty line). */
    public CustomQueue() {
        front = null;
        rear = null;
        size = 0;
    }

    /**
     * Adds an item to the back of the queue (like a new person joining the line).
     * This is also called "offer" or "add" in some queue implementations.
     */
    public void enqueue(T element) {
        Node<T> newNode = new Node<>(element);
        if (isEmpty()) {
            // If the line is empty, this person is both first and last
            front = rear = newNode;
        } else {
            // Add this person behind the current last person
            rear.next = newNode;
            rear = newNode;    // This person is now the last in line
        }
        size++;
    }

    /**
     * Removes and returns the item at the front of the queue.
     * This is like serving the first person in line - they leave the queue.
     * If the queue is empty, an exception is thrown (can't serve nobody!).
     */
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        T data = front.data;       // Get the front person's data
        front = front.next;         // The next person becomes the new front
        if (front == null) {
            rear = null;             // Queue is now empty
        }
        size--;
        return data;
    }

    /**
     * Looks at (but does NOT remove) the item at the front of the queue.
     * This is like checking who is next in line without serving them.
     */
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        return front.data;
    }

    /** Returns how many items are in the queue. */
    public int size() {
        return size;
    }

    /** Returns true if the queue has no items. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Removes all items from the queue (clears the line). */
    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        Node<T> current = front;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}