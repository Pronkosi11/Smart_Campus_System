package datastructures;

import java.util.EmptyStackException;

/**
 * CustomStack - Last-In-First-Out (LIFO) Stack Implementation
 * 
 * A stack is like a pile of plates in a cafeteria.
 * You can only add (push) a new plate on top of the pile.
 * You can only remove (pop) the top plate.
 * The last plate you added is the first one you can take (Last In, First Out).
 * 
 * Real-life examples:
 * - Stack of plates in a cafeteria
 * - Back button in a web browser (last page visited is first to go back to)
 * - Undo/redo operations (last action is first to undo)
 * - Evaluating mathematical expressions
 * 
 * This stack uses a linked list structure, so it can grow as needed.
 */
public class CustomStack<T> {
    
    /** The top of the stack (the plate on top of the pile) */
    private Node<T> top;
    
    /** How many plates (items) are in the stack */
    private int size;

    /**
     * A Node is a single plate in our pile.
     * Each plate knows which plate is below it (next).
     */
    private static class Node<T> {
        /** The data/value stored on this plate */
        T data;
        
        /** The plate below this one in the stack */
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    /** Creates a new empty stack (no plates in the pile). */
    public CustomStack() {
        top = null;
        size = 0;
    }

    /**
     * Pushes (adds) an item onto the top of the stack.
     * Like placing a new plate on top of the pile.
     */
    public void push(T element) {
        Node<T> newNode = new Node<>(element);
        newNode.next = top;     // New plate points to the old top plate
        top = newNode;          // New plate becomes the new top
        size++;
    }

    /**
     * Pops (removes) the top item from the stack and returns it.
     * Like taking the top plate off the pile.
     * If the stack is empty, throws an exception (can't take from an empty pile!).
     */
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T data = top.data;      // Get the data from the top plate
        top = top.next;         // The plate below becomes the new top
        size--;
        return data;
    }

    /**
     * Looks at (but does NOT remove) the top item of the stack.
     * Like looking at the top plate without picking it up.
     */
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return top.data;
    }

    /** Returns how many items are in the stack. */
    public int size() {
        return size;
    }

    /** Returns true if the stack has no items. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Removes all items from the stack (clears the pile). */
    public void clear() {
        top = null;
        size = 0;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        Node<T> current = top;
        CustomStack<T> tempStack = new CustomStack<>();

        // Reverse the stack for display
        while (current != null) {
            tempStack.push(current.data);
            current = current.next;
        }

        while (!tempStack.isEmpty()) {
            sb.append(tempStack.pop());
            if (!tempStack.isEmpty()) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}