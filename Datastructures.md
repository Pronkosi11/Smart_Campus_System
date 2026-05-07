# Custom Data Structures Implementation Guide

This document explains in detail how each custom data structure is implemented in the `datastructures` module and how they are used across the application (`model`, `service`, and `persistence` modules).

---

## Table of Contents

1. [Overview](#overview)
2. [CustomArrayList](#customarraylist)
3. [CustomHashMap](#customhashmap)
4. [CustomLinkedList](#customlinkedlist)
5. [CustomQueue](#customqueue)
6. [CustomStack](#customstack)
7. [Module-Level Usage](#module-level-usage)

---

## Overview

The Smart Campus System implements five custom data structures from scratch to demonstrate core computer science concepts:

| Data Structure | File | Core Concept | Time Complexity (average) |
|---|---|---|---|
| CustomArrayList | `CustomArrayList.java` | Dynamic array with automatic resizing | Access: O(1), Insert/Delete: O(n) |
| CustomHashMap | `CustomHashMap.java` | Hash table with separate chaining | Put/Get/Remove: O(1) |
| CustomLinkedList | `CustomLinkedList.java` | Doubly linked list with head/tail | Access: O(n), Insert/Delete: O(1)* |
| CustomQueue | `CustomQueue.java` | FIFO queue using linked nodes | Enqueue/Dequeue: O(1) |
| CustomStack | `CustomStack.java` | LIFO stack using linked nodes | Push/Pop: O(1) |

---

## CustomArrayList

**File:** `src/datastructures/CustomArrayList.java`

### Internal Design

- **Storage:** Uses a plain `Object[] elements` array as the backing store.
- **Size tracking:** An `int size` field tracks how many elements are actually in use (not the array capacity).
- **Default capacity:** `10` elements when constructed with no arguments.
- **Generic type:** Declared as `CustomArrayList<T> implements Iterable<T>` for type safety and enhanced for-loop support.

### Key Algorithms

**Dynamic Resizing (`ensureCapacity`)**

When `add` is called and `size + 1` exceeds the current array length, the internal array is replaced with a new array that is **double** the old capacity. `System.arraycopy` copies all existing elements into the new array in a single bulk operation.

```
oldCapacity = elements.length
newCapacity = oldCapacity * 2
newElements = new Object[newCapacity]
System.arraycopy(elements, 0, newElements, 0, size)
elements = newElements
```

**Insertion at Index (`add(int index, T element)`)**

1. Validates index bounds (`0 <= index <= size`).
2. Ensures capacity (resizes if needed).
3. Uses `System.arraycopy(elements, index, elements, index + 1, size - index)` to shift elements right by one slot.
4. Inserts the new element and increments `size`.

**Removal by Index (`remove(int index)`)**

1. Validates index bounds (`0 <= index < size`).
2. Uses `System.arraycopy(elements, index + 1, elements, index, size - index - 1)` to shift elements left by one slot.
3. Sets `elements[--size] = null` to avoid memory leaks.
4. Returns the removed element.

**Search (`indexOf`)**

Performs a linear scan from index `0` to `size - 1`, comparing each element with `equals()`. Returns `-1` if not found.

**Iteration**

Implements `Iterator<T>` with an anonymous inner class. A `currentIndex` advances on each `next()` call. `hasNext()` returns `currentIndex < size`.

---

## CustomHashMap

**File:** `src/datastructures/CustomHashMap.java`

### Internal Design

- **Buckets:** An array `Entry<K,V>[] buckets` where each slot is the head of a linked list.
- **Entry node:** A private static inner class `Entry<K,V>` storing `final K key`, `V value`, and `Entry<K,V> next`.
- **Collision resolution:** **Separate chaining** — multiple keys that hash to the same bucket index are stored as a linked list.
- **Load factor:** `0.75f`. When `size >= buckets.length * 0.75`, the table **doubles** in capacity and all entries are rehashed.

### Key Algorithms

**Hash Function (`hash(K key)`)**

```
return Math.abs(key.hashCode() % buckets.length)
```

This guarantees a non-negative index within the bucket array bounds.

**Put Operation (`put(K key, V value)`)**

1. Rejects `null` keys with `IllegalArgumentException`.
2. Triggers `resize()` if the load factor threshold is exceeded.
3. Computes `index = hash(key)`.
4. Scans the linked list at that bucket. If an existing entry has the same key (via `equals()`), its value is overwritten.
5. If the key is not found, a new `Entry` is created and inserted at the **head** of the bucket (`newEntry.next = buckets[index]; buckets[index] = newEntry`).
6. `size` is incremented.

**Get Operation (`get(K key)`)**

1. Computes `index = hash(key)`.
2. Walks the linked list at that bucket until `entry.key.equals(key)`.
3. Returns the matching value, or `null` if the key is absent.

**Resize (`resize()`)**

1. Stores the old bucket array.
2. Allocates a new array with `oldBuckets.length * 2` capacity.
3. Resets `size` to `0`.
4. Re-inserts every entry by calling `put(entry.key, entry.value)` so each entry is rehashed into the new bucket array.

**Key/Value Collection Views**

- `keySet()` and `values()` iterate across every bucket and every node in each linked list, collecting keys or values into a `CustomArrayList`.

---

## CustomLinkedList

**File:** `src/datastructures/CustomLinkedList.java`

### Internal Design

- **Node structure:** A private static inner class `Node<T>` with `T data`, `Node<T> next`, and `Node<T> prev`.
- **Double linking:** Because every node stores both `next` and `prev`, the list can be traversed forward from `head` or backward from `tail`.
- **Boundary pointers:** `head` (first node) and `tail` (last node) are maintained for O(1) access to both ends.

### Key Algorithms

**Insertion at Head (`addFirst`)**

1. Creates a new `Node`.
2. If the list is empty, `head = tail = newNode`.
3. Otherwise:
   - `newNode.next = head`
   - `head.prev = newNode`
   - `head = newNode`

**Insertion at Tail (`addLast`)**

1. Creates a new `Node`.
2. If the list is empty, `head = tail = newNode`.
3. Otherwise:
   - `tail.next = newNode`
   - `newNode.prev = tail`
   - `tail = newNode`

**Insertion in Middle (`add(int index, T element)`)**

1. If `index == 0`, delegates to `addFirst`.
2. If `index == size`, delegates to `addLast`.
3. Otherwise calls `getNode(index)` to reach the target position, then rewires four pointers:
   - `newNode.prev = current.prev`
   - `newNode.next = current`
   - `current.prev.next = newNode`
   - `current.prev = newNode`

**Optimized Node Lookup (`getNode`)**

Because the list is doubly linked, `getNode` chooses the shorter traversal:
- If `index < size / 2`, starts at `head` and moves `next`.
- Otherwise, starts at `tail` and moves `prev`.
This reduces the worst-case traversal from `n` to `n/2`.

**Removal by Value (`remove(T element)`)**

Scans from `head` to `tail`. If the target node is found:
- Head node: delegates to `removeFirst()`.
- Tail node: delegates to `removeLast()`.
- Middle node: rewires neighbors (`current.prev.next = current.next; current.next.prev = current.prev`) and decrements `size`.

---

## CustomQueue

**File:** `src/datastructures/CustomQueue.java`

### Internal Design

- **FIFO discipline:** First element inserted is the first element removed.
- **Linked nodes:** Uses a singly linked node structure (`Node<T>` with `data` and `next`).
- **Boundary pointers:** `front` (head of the line) and `rear` (tail of the line).

### Key Algorithms

**Enqueue (`enqueue`)**

1. Creates a new `Node`.
2. If empty, `front = rear = newNode`.
3. Otherwise:
   - `rear.next = newNode`
   - `rear = newNode`
4. Increments `size`.

**Dequeue (`dequeue`)**

1. Throws `NoSuchElementException` if empty.
2. Reads `front.data`.
3. Moves `front = front.next`.
4. If `front` becomes `null`, also sets `rear = null` (queue is now empty).
5. Decrements `size` and returns the saved data.

**Peek**

Returns `front.data` without removing it. Throws if empty.

---

## CustomStack

**File:** `src/datastructures/CustomStack.java`

### Internal Design

- **LIFO discipline:** Last element pushed is the first element popped.
- **Linked nodes:** Uses a singly linked node structure (`Node<T>` with `data` and `next`).
- **Top pointer:** Only `top` is maintained; each node points to the node below it.

### Key Algorithms

**Push (`push`)**

1. Creates a new `Node`.
2. `newNode.next = top`
3. `top = newNode`
4. Increments `size`.

**Pop (`pop`)**

1. Throws `EmptyStackException` if empty.
2. Reads `top.data`.
3. Moves `top = top.next`.
4. Decrements `size` and returns the saved data.

**Peek**

Returns `top.data` without removal. Throws if empty.

**toString**

Because the natural linked order is top-to-bottom (newest first), `toString` reverses the display by copying elements into a temporary `CustomStack` so the oldest element appears first in the output string.

---

## Module-Level Usage

### 1. DataStructures Module (`src/datastructures/`)

This module contains the raw implementations of the five structures described above. They are written from scratch without extending Java Collections Framework classes, demonstrating:
- Manual memory management (resizing, nulling references)
- Pointer manipulation (linked list rewiring)
- Hashing and collision handling
- Iterator protocol implementation

### 2. Model Module (`src/model/`)

The model layer embeds custom structures directly into entity fields.

**Student.java**
- `CustomArrayList<String> enrolledCourses` — stores course codes the student is registered for.
- `CustomArrayList<String> borrowedBooks` — stores library book IDs currently on loan.
- Methods `enrollCourse(String)` and `dropCourse(String)` use `contains()` to prevent duplicates and `add()`/`remove()` to mutate the list.

**Ticket.java**
- `CustomStack<String> statusHistory` — records every status change as a timestamped string.
- `updateStatus(String)` pushes a new history entry onto the stack via `statusHistory.push(...)`.
- The LIFO nature of the stack means the most recent status change is always on top.

**Course.java, Event.java, HostelRoom.java**
- `CustomArrayList<String>` fields track enrolled student IDs, attendee IDs, and occupant IDs respectively.

### 3. Service Module (`src/service/`)

The service layer uses custom structures as the primary in-memory databases.

**StudentService.java**
- `CustomHashMap<String, Student> students` — primary storage keyed by `studentNumber`.
- `addStudent`, `getStudent`, `updateStudent`, and `deleteStudent` all operate on this map.
- `getAllStudents()` returns `students.values()`, which is a `CustomArrayList<Student>`.
- `getStudentsByFaculty()` iterates over `students.values()` and filters into a new `CustomArrayList`.

**CourseService.java**
- `CustomHashMap<String, Course> courses` — primary storage keyed by `courseCode`.
- `getAvailableCourses()` iterates over `courses.values()` and copies non-full courses into a new `CustomArrayList<Course>`.
- Enrollment logic uses the map for O(1) course lookup and then updates the student's `CustomArrayList` of enrolled courses.

**HelpDeskService.java**
- `CustomQueue<Ticket> pendingTickets` — open and in-progress tickets are held in FIFO order so the oldest ticket is processed first.
- `CustomArrayList<Ticket> resolvedTickets` — closed tickets are archived in a list for reporting.
- `submitTicket()` calls `pendingTickets.enqueue(t)`.
- `getNextTicket()` calls `pendingTickets.peek()`.
- `updateTicketStatus()` removes a ticket from the queue and moves it to `resolvedTickets` when the status becomes `"closed"`.
- `getAllTickets()` merges a snapshot of the pending queue with the resolved list into a single `CustomArrayList<Ticket>`.
- `getTicketHistory()` returns the `CustomStack<String>` stored inside the `Ticket` model.
- `snapshotPendingTickets()` and `snapshotStack()` use temporary queues/stacks to read data without destroying the original structure.

**LibraryService.java & HostelService.java**
- `CustomHashMap<String, LibraryBook>` and `CustomHashMap<String, HostelRoom>` serve as primary storage.
- `LibraryBook` waiting lists are modeled with `CustomQueue<String>`.

**EventService.java**
- `CustomArrayList<Event>` stores the event catalog because events are typically listed sequentially rather than looked up by key.

### 4. Persistence Module (`src/persistence/`)

**DataPersistence.java**

This module bridges the custom in-memory structures with JSON files. It demonstrates deep interaction with all five structures:

**Saving Students**
- Receives `CustomHashMap<String, Student> students`.
- Calls `students.values()` to get a `CustomArrayList<Student>`.
- Iterates with a `for` loop using `vals.get(i)` to transform each `Student` into a JSON-compatible map.

**Loading Students**
- Parses JSON into a list of maps.
- Creates `CustomHashMap<String, Student> map`.
- For each parsed row, creates a `Student` and calls `map.put(s.getStudentNumber(), s)`.
- Assigns the map to `StudentService` via `setStudents(map)`.

**Saving / Loading Tickets**
- `saveTickets` receives `CustomQueue<Ticket> pending` and `CustomArrayList<Ticket> resolved`.
- To read the queue without destroying it, the code uses a **temp-queue pattern**:
  ```java
  CustomQueue<Ticket> temp = new CustomQueue<>();
  while (!pending.isEmpty()) {
      Ticket t = pending.dequeue();
      pendingArr.add(ticketToMap(t));
      temp.enqueue(t);
  }
  while (!temp.isEmpty()) {
      pending.enqueue(temp.dequeue());
  }
  ```
- Loading reconstructs the `CustomQueue<Ticket>` and `CustomArrayList<Ticket>` from JSON arrays.

**Stack Serialization**
- `stackToJsonList` and `jsonListToStack` convert `CustomStack<String>` to/from JSON arrays using a temp-stack pattern, ensuring the original stack is preserved after reading.

**Queue Serialization (Library Waiting Lists)**
- `snapshotQueueIds` dequeues every element from a `CustomQueue<String>` into a `CustomArrayList<String>`, then restores the original queue using a temporary queue.

---

## Summary

| Module | Structures Used | Purpose |
|---|---|---|
| `datastructures` | All five | Raw implementations |
| `model` | `CustomArrayList`, `CustomStack` | Entity fields (lists of IDs, status history) |
| `service` | `CustomHashMap`, `CustomArrayList`, `CustomQueue`, `CustomStack` | In-memory databases, FIFO processing, history tracking |
| `persistence` | All five | JSON serialization/deserialization with temp-structure patterns |

Each structure was built manually to demonstrate the underlying mechanics of arrays, linked lists, hashing, queues, and stacks, while the rest of the application treats them as first-class collections for real domain operations.
