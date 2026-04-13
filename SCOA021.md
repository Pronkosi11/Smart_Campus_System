# Smart Campus Management System ERD



# Smart Campus Management System - Technical Design Document
## 1. Overview
The Smart Campus Management System is a comprehensive Java-based console application designed to streamline campus operations for educational institutions. The system provides role-based access for administrators and students, enabling efficient management of student records, course registration, library resources, hostel allocation, help-desk support, and event bookings.

This document outlines the technical architecture, data models, implementation strategy, and project organization for a menu-driven application that demonstrates proficiency in object-oriented programming principles and custom data structure implementations.

---

## 2. Goals and Non-Goals
### 2.1 Goals
- **Role-Based Authentication**: Implement secure login system distinguishing between admin and student users with appropriate access controls
- **Modular Architecture**: Design a clean, maintainable package structure separating concerns across authentication, modules, models, data structures, and utilities
- **Custom Data Structure Implementation**: Demonstrate understanding of fundamental data structures by implementing custom versions of ArrayList, LinkedList, HashMap, TreeMap, Queue, and Stack
- **CRUD Operations**: Provide complete Create, Read, Update, Delete functionality for all campus entities
- **Menu-Driven Interface**: Deliver intuitive console-based navigation for both user roles
- **In-Memory Data Management**: Implement efficient data storage and retrieval using appropriate data structures for each module
### 2.2 Non-Goals
- **Persistent Storage**: Database integration or file-based persistence is out of scope for initial implementation
- **GUI Interface**: Graphical user interface development is not included
- **Network Features**: Multi-user concurrent access or client-server architecture is not planned
- **Advanced Security**: Encryption, password hashing, or session management beyond basic authentication
- **External Integrations**: Third-party API connections or external service integrations
---

## 3. Architecture
### 3.1 High-Level Architecture
```
┌─────────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────────┐ │
│  │  Main.java  │  │  Menu.java  │  │     ConsoleUtils.java       │ │
│  └─────────────┘  └─────────────┘  └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       AUTHENTICATION LAYER                          │
│  ┌─────────────────┐  ┌──────────┐  ┌─────────┐  ┌───────────────┐ │
│  │ LoginManager    │  │  User    │  │  Admin  │  │    Student    │ │
│  └─────────────────┘  └──────────┘  └─────────┘  └───────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         BUSINESS LAYER                              │
│  ┌────────────────────┐  ┌────────────────────────────────────────┐│
│  │ StudentRecords     │  │ CourseRegistrationModule               ││
│  │ Module             │  │                                        ││
│  └────────────────────┘  └────────────────────────────────────────┘│
│  ┌────────────────────┐  ┌────────────────────────────────────────┐│
│  │ LibraryModule      │  │ HostelModule                          ││
│  └────────────────────┘  └────────────────────────────────────────┘│
│  ┌────────────────────┐  ┌────────────────────────────────────────┐│
│  │ HelpDeskModule     │  │ EventBookingModule                    ││
│  └────────────────────┘  └────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          DATA LAYER                                 │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                    DataStore.java (Singleton)                │   │
│  └─────────────────────────────────────────────────────────────┘   │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────────┐   │
│  │ CustomArrayList│  │ CustomHashMap  │  │ CustomTreeMap      │   │
│  └────────────────┘  └────────────────┘  └────────────────────┘   │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────────┐   │
│  │ CustomLinkedList│ │ CustomQueue    │  │ CustomStack        │   │
│  └────────────────┘  └────────────────┘  └────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```
### 3.2 Package Structure
```
src/
├── main/
│   ├── Main.java                         // Application entry point
│   ├── User.java                         // Abstract base user class
│   ├── Admin.java                        // Admin user implementation
│   ├── Student.java                      // Student user implementation
│   ├── LoginManager.java                 // Authentication logic
│   ├── Menu.java                         // Menu display and routing
│   │
│   ├── modules/
│   │   ├── StudentRecordsModule.java     // Student data management
│   │   ├── CourseRegistrationModule.java // Course enrollment handling
│   │   ├── LibraryModule.java            // Book borrowing system
│   │   ├── HostelModule.java             // Room allocation system
│   │   ├── HelpDeskModule.java           // Ticket management
│   │   └── EventBookingModule.java       // Event registration
│   │
│   ├── datastructures/
│   │   ├── CustomArrayList.java          // Dynamic array implementation
│   │   ├── CustomLinkedList.java         // Linked list implementation
│   │   ├── CustomHashMap.java            // Hash table implementation
│   │   ├── CustomTreeMap.java            // BST-based sorted map
│   │   ├── CustomQueue.java              // FIFO queue implementation
│   │   └── CustomStack.java              // LIFO stack implementation
│   │
│   ├── models/
│   │   ├── StudentRecord.java            // Student entity
│   │   ├── Course.java                   // Course entity
│   │   ├── LibraryBook.java              // Book entity
│   │   ├── HostelRoom.java               // Room entity
│   │   ├── Ticket.java                   // Help-desk ticket entity
│   │   └── Event.java                    // Event entity
│   │
│   └── utils/
│       ├── ConsoleUtils.java             // I/O helper methods
│       └── DataStore.java                // Centralized data storage
```
---

## 4. Data Model
### 4.1 Entity Relationship Overview
The system manages the following core entities with their relationships:

| Entity | Primary Key | Key Relationships |
| ----- | ----- | ----- |
| **Users** | `id`  | Links to Students (1:1 for student role) |
| **Students** | `id`  | Enrolls in Courses (M:N), Borrows Books (M:N), Assigned to Rooms (M:1), Submits Tickets (1:N), Attends Events (M:N) |
| **Courses** | `id`  | Has enrolled Students (M:N) |
| **LibraryBooks** | `id`  | Borrowed by Student (M:1), Has waiting list (M:N) |
| **HostelRooms** | `id`  | Has occupant Students (1:N) |
| **Tickets** | `id`  | Submitted by Student (M:1) |
| **Events** | `id`  | Has attendee Students (M:N) |
### 4.2 Model Class Definitions
#### User.java (Abstract Base Class)
```java
public abstract class User {
    protected String id;
    protected String username;
    protected String password;
    protected String role;  // "admin" or "student"
    
    public abstract boolean hasPermission(String action);
}
```
#### StudentRecord.java
```java
public class StudentRecord {
    private String id;
    private String name;
    private String email;
    private String phone;
    private List<String> enrolledCourseIds;
    private List<String> borrowedBookIds;
    private String hostelRoomId;
    private List<String> helpDeskTicketIds;
    private List<String> eventIds;
}
```
#### Course.java
```java
public class Course {
    private String id;
    private String code;
    private String name;
    private String description;
    private int credits;
    private List<String> enrolledStudentIds;
}
```
#### LibraryBook.java
```java
public class LibraryBook {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private boolean isAvailable;
    private String borrowedByStudentId;
    private Queue<String> waitingListStudentIds;
}
```
#### HostelRoom.java
```java
public class HostelRoom {
    private String id;
    private String roomNumber;
    private int capacity;
    private List<String> occupantStudentIds;
    private boolean isAvailable;
}
```
#### Ticket.java
```java
public class Ticket {
    private String id;
    private String studentId;
    private String subject;
    private String description;
    private String status;  // "open", "in-progress", "closed"
    private Timestamp createdAt;
    private Timestamp resolvedAt;
    private Stack<String> statusHistory;
}
```
#### Event.java
```java
public class Event {
    private String id;
    private String name;
    private String description;
    private Date date;
    private String location;
    private List<String> attendeeStudentIds;
}
```
### 4.3 Data Structure Mapping by Module
| Module | Primary Data Structure | Justification |
| ----- | ----- | ----- |
| **Student Records** | `CustomHashMap<String, StudentRecord>`  | O(1) lookup by student ID |
| **Course Registration** | `CustomHashMap<String, Course>`  | Fast course code lookup |
| **Library** | `CustomHashMap<String, LibraryBook>` + `CustomQueue<String>`  | Book lookup + FIFO waiting list |
| **Hostel** | `CustomTreeMap<String, HostelRoom>`  | Sorted room number ordering |
| **Help Desk** | `CustomQueue<Ticket>` + `CustomStack<String>`  | FIFO ticket processing + status history |
| **Events** | `CustomTreeMap<Date, Event>`  | Chronological event ordering |
---

## 5. API Design
### 5.1 Module Interface Specifications
#### StudentRecordsModule
```java
public class StudentRecordsModule {
    // Admin operations
    public void addStudent(StudentRecord student);
    public StudentRecord getStudent(String studentId);
    public List<StudentRecord> getAllStudents();
    public void updateStudent(String studentId, StudentRecord updated);
    public void deleteStudent(String studentId);
    public List<StudentRecord> searchByName(String name);
}
```
#### CourseRegistrationModule
```java
public class CourseRegistrationModule {
    // Admin operations
    public void addCourse(Course course);
    public void updateCourse(String courseId, Course updated);
    public void deleteCourse(String courseId);
    
    // Student operations
    public void registerStudent(String studentId, String courseId);
    public void dropCourse(String studentId, String courseId);
    public List<Course> getStudentCourses(String studentId);
    public List<Course> getAvailableCourses();
}
```
#### LibraryModule
```java
public class LibraryModule {
    // Admin operations
    public void addBook(LibraryBook book);
    public void removeBook(String bookId);
    public List<LibraryBook> getAllBooks();
    
    // Student operations
    public boolean borrowBook(String studentId, String bookId);
    public void returnBook(String studentId, String bookId);
    public void joinWaitingList(String studentId, String bookId);
    public List<LibraryBook> getBorrowedBooks(String studentId);
    public int getWaitingListPosition(String studentId, String bookId);
}
```
#### HostelModule
```java
public class HostelModule {
    // Admin operations
    public void addRoom(HostelRoom room);
    public void updateRoom(String roomId, HostelRoom updated);
    public List<HostelRoom> getAllRooms();
    public List<HostelRoom> getAvailableRooms();
    
    // Student operations
    public boolean applyForRoom(String studentId, String roomId);
    public void vacateRoom(String studentId);
    public HostelRoom getStudentRoom(String studentId);
}
```
#### HelpDeskModule
```java
public class HelpDeskModule {
    // Admin operations
    public Ticket getNextTicket();
    public void updateTicketStatus(String ticketId, String status);
    public List<Ticket> getAllTickets();
    public List<Ticket> getTicketsByStatus(String status);
    
    // Student operations
    public void submitTicket(String studentId, String subject, String description);
    public List<Ticket> getStudentTickets(String studentId);
    public Stack<String> getTicketHistory(String ticketId);
}
```
#### EventBookingModule
```java
public class EventBookingModule {
    // Admin operations
    public void createEvent(Event event);
    public void updateEvent(String eventId, Event updated);
    public void cancelEvent(String eventId);
    
    // Student operations
    public void registerForEvent(String studentId, String eventId);
    public void cancelRegistration(String studentId, String eventId);
    public List<Event> getUpcomingEvents();
    public List<Event> getStudentEvents(String studentId);
}
```
### 5.2 Menu Flow Specifications
#### Admin Menu Options
```
╔════════════════════════════════════════╗
║        ADMIN DASHBOARD                 ║
╠════════════════════════════════════════╣
║  1. Manage Students                    ║
║  2. Manage Courses                     ║
║  3. Manage Library                     ║
║  4. Manage Hostels                     ║
║  5. View Help Desk Tickets             ║
║  6. Manage Events                      ║
║  7. Logout                             ║
╚════════════════════════════════════════╝
```
#### Student Menu Options
```
╔════════════════════════════════════════╗
║        STUDENT PORTAL                  ║
╠════════════════════════════════════════╣
║  1. View My Profile                    ║
║  2. Course Registration                ║
║  3. Library Services                   ║
║  4. Hostel Application                 ║
║  5. Submit Help Desk Ticket            ║
║  6. Event Booking                      ║
║  7. Logout                             ║
╚════════════════════════════════════════╝
```
---

## 6. Security Considerations
### 6.1 Authentication
- **Credential Storage**: Admin credentials hardcoded for demonstration; student credentials stored in DataStore
- **Login Validation**: Username/password matching with role verification
- **Session Management**: User object maintained in memory during active session
### 6.2 Authorization
| Action Category | Admin | Student |
| ----- | ----- | ----- |
| View all students | ✓ | ✗ |
| Modify student records | ✓ | ✗ |
| Create/delete courses | ✓ | ✗ |
| Register for courses | ✓ | ✓ (own) |
| Add/remove books | ✓ | ✗ |
| Borrow/return books | ✓ | ✓ (own) |
| Manage rooms | ✓ | ✗ |
| Apply for hostel | ✓ | ✓ (own) |
| Process tickets | ✓ | ✗ |
| Submit tickets | ✓ | ✓ (own) |
| Create events | ✓ | ✗ |
| Book events | ✓ | ✓ (own) |
### 6.3 Input Validation
- Validate all user inputs before processing
- Check for null/empty strings
- Verify ID formats and existence before operations
- Prevent duplicate registrations
---

## 7. Testing Strategy
### 7.1 Unit Testing
| Component | Test Focus |
| ----- | ----- |
| **Custom Data Structures** | Add, remove, search, edge cases (empty, full, duplicate) |
| **LoginManager** | Valid/invalid credentials, role assignment |
| **Each Module** | CRUD operations, business logic validation |
| **Models** | Getter/setter functionality, data integrity |
### 7.2 Integration Testing
- **Login → Menu Flow**: Verify correct menu displayed per role
- **Module Interactions**: Course registration updates both student and course records
- **Data Consistency**: Book borrowing updates availability and student records
### 7.3 User Acceptance Testing
| Scenario | Steps | Expected Result |
| ----- | ----- | ----- |
| Admin adds student | Login as admin → Add student → Verify in list | Student appears in records |
| Student borrows book | Login as student → Borrow available book | Book marked unavailable, added to student |
| Ticket processing | Student submits → Admin processes | FIFO order maintained, history tracked |
### 7.4 Test Cases for Custom Data Structures
```java
// CustomHashMap Tests
@Test void testPutAndGet();
@Test void testCollisionHandling();
@Test void testRemove();
@Test void testContainsKey();

// CustomQueue Tests
@Test void testEnqueueDequeue();
@Test void testFIFOOrder();
@Test void testEmptyQueueException();

// CustomStack Tests
@Test void testPushPop();
@Test void testLIFOOrder();
@Test void testPeek();
```
---

## 8. Rollout Plan
### 8.1 Development Phases
#### Phase 1: Requirements & Design (Week 1)
- [ ] Finalize module specifications
- [ ] Complete data structure selection
- [ ] Design class diagrams
- [ ] Create detailed ERD
- [ ] Assign team responsibilities
#### Phase 2: Core Architecture (Week 2)
- [ ] Implement package structure
- [ ] Create Main.java entry point
- [ ] Implement User, Admin, Student classes
- [ ] Build LoginManager
- [ ] Develop Menu system
- [ ] Create DataStore singleton
#### Phase 3: Module Implementation (Weeks 3-4)
- [ ] **Person 1**: Main class, login, menus, DataStore
- [ ] **Person 2**: StudentRecordsModule, CourseRegistrationModule
- [ ] **Person 3**: LibraryModule, HostelModule, HelpDeskModule
- [ ] **Person 4**: EventBookingModule, Custom data structures
#### Phase 4: Testing & Documentation (Week 5)
- [ ] Unit test all components
- [ ] Integration testing
- [ ] Bug fixes and refinements
- [ ] Write technical documentation
- [ ] Prepare demonstration
### 8.2 Team Assignment Matrix
| Team Member | Primary Responsibility | Data Structures Used |
| ----- | ----- | ----- |
| **Person 1** | Main, Login, Menu, DataStore | All (integration) |
| **Person 2** | Student Records, Course Registration | CustomArrayList, CustomHashMap |
| **Person 3** | Library, Hostel, Help Desk | CustomQueue, CustomStack, CustomTreeMap |
| **Person 4** | Event Booking, Custom DS Implementation | CustomLinkedList, CustomTreeMap |
### 8.3 Deliverables Checklist
- [ ] Complete source code with all modules
- [ ] Custom data structure implementations with documentation
- [ ] Test cases and results
- [ ] User manual for admin and student operations
- [ ] Technical report explaining:
    - OOP design decisions
    - Data structure choices and justifications
    - Time complexity analysis
    - Challenges and solutions

### 8.4 Risk Mitigation
| Risk | Mitigation Strategy |
| ----- | ----- |
| Data structure complexity | Start with Java built-in, refactor to custom |
| Integration issues | Daily sync meetings, shared DataStore |
| Scope creep | Strict adherence to defined modules |
| Testing gaps | Parallel testing during development |
---

## 9. Appendix
### 9.1 Custom Data Structure Complexity Analysis
| Data Structure | Insert | Delete | Search | Use Case |
| ----- | ----- | ----- | ----- | ----- |
| CustomArrayList | O(1)* | O(n) | O(n) | Student lists, event attendees |
| CustomLinkedList | O(1) | O(1)** | O(n) | Dynamic collections |
| CustomHashMap | O(1) | O(1) | O(1) | ID-based lookups |
| CustomTreeMap | O(log n) | O(log n) | O(log n) | Sorted data (rooms, dates) |
| CustomQueue | O(1) | O(1) | O(n) | Ticket processing, waiting lists |
| CustomStack | O(1) | O(1) | O(n) | Status history, undo operations |
*Amortized
**With reference to node

### 9.2 Sample DataStore Implementation
```java
public class DataStore {
    private static DataStore instance;
    
    // Collections using custom data structures
    public CustomHashMap<String, StudentRecord> students;
    public CustomHashMap<String, Course> courses;
    public CustomHashMap<String, LibraryBook> libraryBooks;
    public CustomTreeMap<String, HostelRoom> hostelRooms;
    public CustomQueue<Ticket> helpDeskTickets;
    public CustomTreeMap<Date, Event> events;
    
    private DataStore() {
        students = new CustomHashMap<>();
        courses = new CustomHashMap<>();
        libraryBooks = new CustomHashMap<>();
        hostelRooms = new CustomTreeMap<>();
        helpDeskTickets = new CustomQueue<>();
        events = new CustomTreeMap<>();
    }
    
    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }
}
```




