package datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * CustomLinkedList - Doubly Linked List Implementation
 * 
 * A linked list is like a chain of train cars where each car is connected to the next.
 * Unlike an array, items are not stored next to each other in memory.
 * Instead, each item (called a Node) knows where the next item is.
 * 
 * This is a "doubly linked" list because each node also knows where the PREVIOUS item is.
 * This makes it easy to move both forward and backward through the list.
 * 
 * Think of it like a conga line where each person holds hands with the person in front AND behind them.
 * 
 * Key Features:
 * - Adding and removing items is fast (no shifting needed like in an array)
 * - Can easily add items to the front or back
 * - Can move forward and backward through the list
 * - Grows and shrinks automatically
 */
public class CustomLinkedList<T> implements Iterable<T> {
    
    /** The first item in the list (like the head of the train) */
    private Node<T> head;
    
    /** The last item in the list (like the caboose of the train) */
    private Node<T> tail;
    
    /** How many items are in the list */
    private int size;

    /**
     * A Node is a single "train car" in our linked list.
     * Each node holds:
     * - data: the actual value stored (like the cargo in the train car)
     * - next: reference to the next node (the next train car)
     * - prev: reference to the previous node (the previous train car)
     */
    private static class Node<T> {
        /** The actual data/value stored in this node */
        T data;
        
        /** Reference to the next node in the list */
        Node<T> next;
        
        /** Reference to the previous node in the list */
        Node<T> prev;

        Node(T data) {
            this.data = data;
            this.next = null;
            this.prev = null;
        }
    }

    /** Creates a new empty linked list. */
    public CustomLinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Adds an item to the front (beginning) of the list.
     * Think of it like adding a new train car to the front of the train.
     */
    public void addFirst(T element) {
        Node<T> newNode = new Node<>(element);
        if (isEmpty()) {
            // If list is empty, this item is both the first and last
            head = tail = newNode;
        } else {
            // Link the new node to the current head
            newNode.next = head;      // New node points to current first item
            head.prev = newNode;      // Current first item points back to new node
            head = newNode;            // New node becomes the new head
        }
        size++;
    }

    /**
     * Adds an item to the back (end) of the list.
     * Think of it like adding a new train car to the back of the train.
     */
    public void addLast(T element) {
        Node<T> newNode = new Node<>(element);
        if (isEmpty()) {
            // If list is empty, this item is both the first and last
            head = tail = newNode;
        } else {
            // Link the new node to the current tail
            tail.next = newNode;      // Current last item points to new node
            newNode.prev = tail;      // New node points back to current last
            tail = newNode;            // New node becomes the new tail
        }
        size++;
    }

    /** Adds an item to the end of the list (same as addLast). */
    public void add(T element) {
        addLast(element);
    }

    /**
     * Inserts an item at a specific position in the list.
     * This involves "cutting" the chain, inserting the new node, and reconnecting.
     */
    public void add(int index, T element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == 0) {
            addFirst(element);      // Insert at the beginning
        } else if (index == size) {
            addLast(element);       // Insert at the end
        } else {
            // Insert in the middle
            Node<T> current = getNode(index);     // Find the node at this position
            Node<T> newNode = new Node<>(element);
            
            // Reconnect the links to insert the new node
            newNode.prev = current.prev;          // New node connects to previous
            newNode.next = current;                // New node connects to current
            current.prev.next = newNode;           // Previous node connects to new node
            current.prev = newNode;                // Current node connects back to new node
            size++;
        }
    }

    /**
     * Removes and returns the first item in the list.
     * Think of it like removing the first train car from the train.
     */
    public T removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        T data = head.data;
        head = head.next;          // Move head to the next item
        if (head == null) {
            tail = null;             // List is now empty
        } else {
            head.prev = null;        // New head has no previous item
        }
        size--;
        return data;
    }

    /**
     * Removes and returns the last item in the list.
     * Think of it like removing the last train car from the train.
     */
    public T removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        T data = tail.data;
        tail = tail.prev;          // Move tail to the previous item
        if (tail == null) {
            head = null;             // List is now empty
        } else {
            tail.next = null;        // New tail has no next item
        }
        size--;
        return data;
    }

    /**
     * Removes the item at a specific position.
     * We "bypass" this node by making its neighbors connect directly to each other.
     */
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == 0) {
            return removeFirst();
        } else if (index == size - 1) {
            return removeLast();
        } else {
            Node<T> current = getNode(index);         // Find the node to remove
            current.prev.next = current.next;         // Previous skips over current
            current.next.prev = current.prev;         // Next skips back over current
            size--;
            return current.data;
        }
    }

    /**
     * Finds and removes the first matching item from the list.
     * Returns true if found and removed, false if not found.
     */
    public boolean remove(T element) {
        Node<T> current = head;
        while (current != null) {
            if (current.data.equals(element)) {
                if (current == head) {
                    removeFirst();
                } else if (current == tail) {
                    removeLast();
                } else {
                    // Remove from middle by bypassing this node
                    current.prev.next = current.next;
                    current.next.prev = current.prev;
                    size--;
                }
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /** Gets the item at a specific position. */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return getNode(index).data;
    }

    /** Gets the first item in the list. */
    public T getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        return head.data;
    }

    /** Gets the last item in the list. */
    public T getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        return tail.data;
    }

    /**
     * Finds the node at a specific position.
     * Smart trick: if the index is closer to the beginning, start from head.
     * If it's closer to the end, start from tail and go backward.
     * This saves time compared to always starting from the beginning.
     */
    private Node<T> getNode(int index) {
        Node<T> current;
        if (index < size / 2) {
            // Index is in the first half - start from head
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        } else {
            // Index is in the second half - start from tail
            current = tail;
            for (int i = size - 1; i > index; i--) {
                current = current.prev;
            }
        }
        return current;
    }

    /** Checks if an item exists in the list. */
    public boolean contains(T element) {
        return indexOf(element) >= 0;
    }

    /**
     * Finds the position (index) of an item.
     * Returns -1 if the item is not found.
     */
    public int indexOf(T element) {
        Node<T> current = head;
        int index = 0;
        while (current != null) {
            if (current.data.equals(element)) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }

    /** Returns how many items are in the list. */
    public int size() {
        return size;
    }

    /** Returns true if the list has no items. */
    public boolean isEmpty() {
        return size == 0;
    }

    /** Removes all items from the list. */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        Node<T> current = head;
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