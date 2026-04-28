package persistence;

import datastructures.CustomArrayList;
import datastructures.CustomHashMap;
import datastructures.CustomQueue;
import datastructures.CustomStack;
import model.*;
import service.CourseService;
import service.EventService;
import service.HelpDeskService;
import service.HostelService;
import service.LibraryService;
import service.LoginService;
import service.StudentService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DataPersistence - JSON Read/Write Layer
 *
 * Loads and saves every domain object as a JSON file under {@code data/}.
 * The application calls {@link #loadAllData()} once at startup and
 * {@link #saveAllData()} once at shutdown. Individual services also
 * call the per-module save methods (e.g. {@link #saveBooks}) after
 * each change so nothing is lost if the program crashes.
 *
 * File map:
 *   - data/students.json  : student records + their courses, books, hostel
 *   - data/courses.json   : course catalogue + enrollments
 *   - data/books.json     : library catalogue + waiting lists
 *   - data/hostels.json   : hostel catalogue + applications
 *   - data/tickets.json   : help desk pending queue + resolved archive
 *   - data/events.json    : campus events + registered students
 *
 * Beginner notes:
 *  - Each domain has a *_ToMap() and *_FromMap() helper. Maps are easy
 *    to convert to JSON via {@link JsonFileHandler}.
 *  - The class is final + has a private constructor: it is a pure
 *    utility holder (no instances ever exist).
 *  - Schema validation is best-effort: warnings are printed unless the
 *    STRICT_SCHEMA environment variable is set to "true".
 */
public final class DataPersistence {

    // ---- Data file paths (relative to the project root) ----
    public static final String STUDENTS_FILE = "data/students.json";
    public static final String COURSES_FILE = "data/courses.json";
    public static final String BOOKS_FILE = "data/books.json";
    public static final String HOSTELS_FILE = "data/hostels.json";
    public static final String TICKETS_FILE = "data/tickets.json";
    public static final String EVENTS_FILE = "data/events.json";

    // ---- Schema file paths (used by the validator at startup) ----
    public static final String STUDENTS_SCHEMA_FILE = "schemas/students.schema.json";
    public static final String COURSES_SCHEMA_FILE = "schemas/courses.schema.json";
    public static final String BOOKS_SCHEMA_FILE = "schemas/books.schema.json";
    public static final String HOSTELS_SCHEMA_FILE = "schemas/hostels.schema.json";
    public static final String TICKETS_SCHEMA_FILE = "schemas/tickets.schema.json";
    public static final String EVENTS_SCHEMA_FILE = "schemas/events.schema.json";

    // If STRICT_SCHEMA=true is set, schema problems become hard errors.
    private static final boolean STRICT_SCHEMA_VALIDATION =
            "true".equalsIgnoreCase(System.getenv("STRICT_SCHEMA"));

    private DataPersistence() {
        // utility class — no instances
    }

    // ============================================================
    //  TOP-LEVEL ENTRY POINTS
    // ============================================================

    /**
     * Loads every JSON file into the corresponding service. Called once
     * at startup from Main.
     */
    public static void loadAllData() {
        validateConfiguredDataFiles();
        loadStudents();
        loadCourses();
        loadBooks();
        loadHostels();
        loadTickets();
        loadEvents();
    }

    /**
     * Saves every service's current state. Called once on shutdown.
     */
    public static void saveAllData() {
        saveStudents(StudentService.getInstance().getStudentsMap());
        saveCourses(CourseService.getInstance().getCoursesMap());
        saveBooks(LibraryService.getInstance().getBooksMap());
        HostelService hs = HostelService.getInstance();
        saveHostels(hs.getHostelsMap(),
                hs.getPendingApplicationsSnapshotAsQueue(),
                hs.getProcessedApplications());
        HelpDeskService helpDeskService = HelpDeskService.getInstance();
        saveTickets(
                helpDeskService.getPendingTicketsSnapshotAsQueue(),
                helpDeskService.getResolvedTickets(),
                helpDeskService.getTicketCounter()
        );
        saveEvents(EventService.getInstance().getEventsMap());
    }

    // ============================================================
    //  STUDENTS
    // ============================================================

    public static void saveStudents(CustomHashMap<String, Student> students) {
        try {
            List<Object> arr = new ArrayList<>();
            CustomArrayList<Student> vals = students.values();
            for (int i = 0; i < vals.size(); i++) {
                arr.add(studentToMap(vals.get(i)));
            }
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("students", arr);
            JsonFileHandler.writeFile(STUDENTS_FILE, JsonFileHandler.stringifyObject(root));
        } catch (IOException e) {
            System.err.println("Failed to save students: " + e.getMessage());
        }
    }

    private static Map<String, Object> studentToMap(Student s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("studentNumber", s.getStudentNumber());
        m.put("name", s.getName());
        m.put("password", s.getPassword());
        m.put("gender", s.getGender());
        m.put("department", s.getDepartment());
        m.put("year", s.getYear());
        m.put("email", s.getEmail());
        m.put("phone", s.getPhone());
        m.put("enrolledCourses", stringListToJson(s.getEnrolledCourses()));
        m.put("borrowedBooks", stringListToJson(s.getBorrowedBooks()));
        m.put("hostelRoom", s.getHostelRoom());
        m.put("registrationDate", s.getRegistrationDate().toString());
        return m;
    }

    private static void loadStudents() {
        try {
            String raw = JsonFileHandler.readFile(STUDENTS_FILE);
            if (raw == null) {
                return;
            }
            Map<String, Object> root = JsonFileHandler.parseObject(raw);
            List<Map<String, Object>> rows = getObjectList(root, "students");
            CustomHashMap<String, Student> map = new CustomHashMap<>();
            for (Map<String, Object> row : rows) {
                Student s = studentFromMap(row);
                if (s != null) {
                    map.put(s.getStudentNumber(), s);
                }
            }
            StudentService.getInstance().setStudents(map);
            CustomArrayList<Student> all = StudentService.getInstance().getAllStudents();
            for (int i = 0; i < all.size(); i++) {
                LoginService.getInstance().registerStudent(all.get(i));
            }
        } catch (Exception e) {
            System.err.println("Failed to load students: " + e.getMessage());
        }
    }

    private static Student studentFromMap(Map<String, Object> row) {
        String studentNumber = getStr(row, "studentNumber", null);
        // Backward compatibility for older data files that used "id".
        if (studentNumber == null) {
            studentNumber = getStr(row, "id", null);
        }
        if (studentNumber == null) {
            return null;
        }
        Student s = new Student(
                studentNumber,
                getStr(row, "name", ""),
                getStr(row, "password", ""),
                getStr(row, "gender", "Unspecified"),
                getStr(row, "department", "General"),
                getInt(row, "year", 1),
                getStr(row, "email", ""),
                getStr(row, "phone", "")
        );
        CustomArrayList<String> courses = readStringListObj(row.get("enrolledCourses"));
        for (int i = 0; i < courses.size(); i++) {
            s.enrollCourse(courses.get(i));
        }
        CustomArrayList<String> books = readStringListObj(row.get("borrowedBooks"));
        for (int i = 0; i < books.size(); i++) {
            s.borrowBook(books.get(i));
        }
        String hostel = getStr(row, "hostelRoom", null);
        if (hostel != null && !hostel.isEmpty() && !"null".equalsIgnoreCase(hostel)) {
            s.setHostelRoom(hostel);
        }
        String reg = getStr(row, "registrationDate", null);
        if (reg != null && !reg.isEmpty()) {
            try {
                s.setRegistrationDate(LocalDate.parse(reg));
            } catch (Exception ignored) {
                // keep default now()
            }
        }
        return s;
    }

    // ============================================================
    //  COURSES
    // ============================================================

    public static void saveCourses(CustomHashMap<String, Course> courses) {
        try {
            List<Object> arr = new ArrayList<>();
            CustomArrayList<Course> vals = courses.values();
            for (int i = 0; i < vals.size(); i++) {
                arr.add(courseToMap(vals.get(i)));
            }
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("courses", arr);
            JsonFileHandler.writeFile(COURSES_FILE, JsonFileHandler.stringifyObject(root));
        } catch (IOException e) {
            System.err.println("Failed to save courses: " + e.getMessage());
        }
    }

    private static Map<String, Object> courseToMap(Course c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("courseCode", c.getCourseCode());
        m.put("courseName", c.getCourseName());
        m.put("instructor", c.getInstructor());
        m.put("credits", c.getCredits());
        m.put("maxCapacity", c.getMaxCapacity());
        m.put("schedule", c.getSchedule());
        m.put("enrolledStudents", stringListToJson(c.getEnrolledStudents()));
        return m;
    }

    private static void loadCourses() {
        try {
            String raw = JsonFileHandler.readFile(COURSES_FILE);
            if (raw == null) {
                return;
            }
            Map<String, Object> root = JsonFileHandler.parseObject(raw);
            List<Map<String, Object>> rows = getObjectList(root, "courses");
            CustomHashMap<String, Course> map = new CustomHashMap<>();
            for (Map<String, Object> row : rows) {
                Course c = courseFromMap(row);
                if (c != null) {
                    map.put(c.getCourseCode(), c);
                }
            }
            CourseService.getInstance().setCourses(map);
        } catch (Exception e) {
            System.err.println("Failed to load courses: " + e.getMessage());
        }
    }

    private static Course courseFromMap(Map<String, Object> row) {
        String code = getStr(row, "courseCode", null);
        if (code == null) {
            return null;
        }
        Course c = new Course(
                code,
                getStr(row, "courseName", ""),
                getStr(row, "instructor", ""),
                getInt(row, "credits", 0),
                getInt(row, "maxCapacity", 30),
                getStr(row, "schedule", "TBA")
        );
        c.loadEnrolledFromPersistence(readStringListObj(row.get("enrolledStudents")));
        return c;
    }

    // ============================================================
    //  LIBRARY BOOKS
    // ============================================================

    public static void saveBooks(CustomHashMap<String, LibraryBook> books) {
        try {
            List<Object> arr = new ArrayList<>();
            CustomArrayList<LibraryBook> vals = books.values();
            for (int i = 0; i < vals.size(); i++) {
                arr.add(bookToMap(vals.get(i)));
            }
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("books", arr);
            JsonFileHandler.writeFile(BOOKS_FILE, JsonFileHandler.stringifyObject(root));
        } catch (IOException e) {
            System.err.println("Failed to save books: " + e.getMessage());
        }
    }

    private static Map<String, Object> bookToMap(LibraryBook b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bookId", b.getBookId());
        m.put("title", b.getTitle());
        m.put("author", b.getAuthor());
        m.put("isbn", b.getIsbn());
        m.put("totalCopies", b.getTotalCopies());
        m.put("availableCopies", b.getAvailableCopies());
        m.put("waitingList", queueToJsonList(b.getWaitingList()));
        return m;
    }

    private static void loadBooks() {
        try {
            String raw = JsonFileHandler.readFile(BOOKS_FILE);
            if (raw == null || raw.isBlank()) {
                return;
            }
            Map<String, Object> root = JsonFileHandler.parseObject(raw);
            List<Map<String, Object>> rows = getObjectList(root, "books");
            CustomHashMap<String, LibraryBook> map = new CustomHashMap<>();
            for (Map<String, Object> row : rows) {
                LibraryBook b = bookFromMap(row);
                if (b != null) {
                    map.put(b.getBookId(), b);
                }
            }
            LibraryService.getInstance().setBooks(map);
        } catch (Exception e) {
            System.err.println("Failed to load books: " + e.getMessage());
        }
    }

    private static LibraryBook bookFromMap(Map<String, Object> row) {
        String id = getStr(row, "bookId", null);
        if (id == null) {
            return null;
        }
        int totalCopies = getInt(row, "totalCopies", 1);
        int availableCopies = getInt(row, "availableCopies", totalCopies);
        LibraryBook b = new LibraryBook(
                id,
                getStr(row, "title", ""),
                getStr(row, "author", ""),
                getStr(row, "isbn", "N/A"),
                totalCopies
        );
        b.restoreInventory(totalCopies, availableCopies);

        // Rebuild the waiting queue in original order.
        CustomQueue<String> waiting = new CustomQueue<>();
        CustomArrayList<String> rawList = readStringListObj(row.get("waitingList"));
        for (int i = 0; i < rawList.size(); i++) {
            waiting.enqueue(rawList.get(i));
        }
        b.setWaitingList(waiting);
        return b;
    }

    // ============================================================
    //  HOSTELS  (catalogue + applications)
    // ============================================================

    public static void saveHostels(CustomHashMap<String, HostelRoom> hostels,
                                   CustomQueue<HostelApplication> pending,
                                   CustomArrayList<HostelApplication> processed) {
        try {
            List<Object> hostelArr = new ArrayList<>();
            CustomArrayList<HostelRoom> vals = hostels.values();
            for (int i = 0; i < vals.size(); i++) {
                hostelArr.add(hostelToMap(vals.get(i)));
            }

            // Walk the queue carefully so we don't lose applications.
            List<Object> pendingArr = new ArrayList<>();
            if (pending != null) {
                CustomQueue<HostelApplication> temp = new CustomQueue<>();
                while (!pending.isEmpty()) {
                    HostelApplication a = pending.dequeue();
                    pendingArr.add(applicationToMap(a));
                    temp.enqueue(a);
                }
                while (!temp.isEmpty()) {
                    pending.enqueue(temp.dequeue());
                }
            }

            List<Object> processedArr = new ArrayList<>();
            if (processed != null) {
                for (int i = 0; i < processed.size(); i++) {
                    processedArr.add(applicationToMap(processed.get(i)));
                }
            }

            Map<String, Object> root = new LinkedHashMap<>();
            root.put("hostels", hostelArr);
            root.put("applications", pendingArr);
            root.put("history", processedArr);
            JsonFileHandler.writeFile(HOSTELS_FILE, JsonFileHandler.stringifyObject(root));
        } catch (IOException e) {
            System.err.println("Failed to save hostels: " + e.getMessage());
        }
    }

    private static Map<String, Object> hostelToMap(HostelRoom h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", h.getName());
        m.put("code", h.getCode());
        m.put("unitRange", h.getUnitRange());
        m.put("allocationGroup", h.getAllocationGroup());
        m.put("capacity", h.getCapacity());
        m.put("occupants", stringListToJson(h.getOccupants()));
        return m;
    }

    private static Map<String, Object> applicationToMap(HostelApplication a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("studentNumber", a.getStudentNumber());
        m.put("hostelName", a.getHostelName());
        m.put("status", a.getStatus());
        m.put("appliedAt", a.getAppliedAt() == null ? null : a.getAppliedAt().toString());
        return m;
    }

    private static void loadHostels() {
        try {
            String raw = JsonFileHandler.readFile(HOSTELS_FILE);
            if (raw == null || raw.isBlank()) {
                return;
            }
            Map<String, Object> root = JsonFileHandler.parseObject(raw);
            List<Map<String, Object>> rows = getObjectList(root, "hostels");
            CustomHashMap<String, HostelRoom> map = new CustomHashMap<>();
            for (Map<String, Object> row : rows) {
                HostelRoom h = hostelFromMap(row);
                if (h != null) {
                    map.put(h.getName(), h);
                }
            }
            HostelService.getInstance().setHostels(map);

            // Load applications (pending queue + processed history).
            CustomQueue<HostelApplication> pending = new CustomQueue<>();
            CustomArrayList<HostelApplication> processed = new CustomArrayList<>();
            for (Map<String, Object> row : getObjectList(root, "applications")) {
                HostelApplication a = applicationFromMap(row);
                if (a != null) {
                    pending.enqueue(a);
                }
            }
            for (Map<String, Object> row : getObjectList(root, "history")) {
                HostelApplication a = applicationFromMap(row);
                if (a != null) {
                    processed.add(a);
                }
            }
            HostelService.getInstance().restoreApplications(pending, processed);
        } catch (Exception e) {
            System.err.println("Failed to load hostels: " + e.getMessage());
        }
    }

    private static HostelRoom hostelFromMap(Map<String, Object> row) {
        String name = getStr(row, "name", null);
        if (name == null) {
            return null;
        }
        HostelRoom h = new HostelRoom(
                name,
                getStr(row, "code", null),
                getStr(row, "unitRange", null),
                getStr(row, "allocationGroup", "Undergraduates - Mixed"),
                getInt(row, "capacity", 100)
        );
        // Restore occupants list.
        CustomArrayList<String> occupants = readStringListObj(row.get("occupants"));
        h.setOccupants(occupants);
        return h;
    }

    private static HostelApplication applicationFromMap(Map<String, Object> row) {
        String studentNumber = getStr(row, "studentNumber", null);
        String hostelName = getStr(row, "hostelName", null);
        if (studentNumber == null || hostelName == null) {
            return null;
        }
        String status = getStr(row, "status", HostelApplication.STATUS_PENDING);
        LocalDateTime appliedAt = null;
        String appliedRaw = getStr(row, "appliedAt", null);
        if (appliedRaw != null && !appliedRaw.isBlank()) {
            try {
                appliedAt = LocalDateTime.parse(appliedRaw);
            } catch (Exception ignored) {
                // fall back to "now"
            }
        }
        return new HostelApplication(studentNumber, hostelName, status, appliedAt);
    }

    // ============================================================
    //  TICKETS
    // ============================================================

    public static void saveTickets(CustomQueue<Ticket> pending,
                                   CustomArrayList<Ticket> resolved,
                                   int ticketCounter) {
        try {
            List<Object> pendingArr = new ArrayList<>();
            List<Object> resolvedArr = new ArrayList<>();

            if (pending != null) {
                CustomQueue<Ticket> temp = new CustomQueue<>();
                while (!pending.isEmpty()) {
                    Ticket t = pending.dequeue();
                    pendingArr.add(ticketToMap(t));
                    temp.enqueue(t);
                }
                while (!temp.isEmpty()) {
                    pending.enqueue(temp.dequeue());
                }
            }

            if (resolved != null) {
                for (int i = 0; i < resolved.size(); i++) {
                    resolvedArr.add(ticketToMap(resolved.get(i)));
                }
            }

            Map<String, Object> root = new LinkedHashMap<>();
            root.put("pending", pendingArr);
            root.put("resolved", resolvedArr);
            root.put("ticketCounter", Math.max(ticketCounter, 1000));
            JsonFileHandler.writeFile(TICKETS_FILE, JsonFileHandler.stringifyObject(root));
        } catch (IOException e) {
            System.err.println("Failed to save tickets: " + e.getMessage());
        }
    }

    private static Map<String, Object> ticketToMap(Ticket t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("studentNumber", t.getStudentNumber());
        m.put("subject", t.getSubject());
        m.put("description", t.getDescription());
        m.put("status", t.getStatus());
        m.put("createdAt", t.getCreatedAt() == null ? null : t.getCreatedAt().toString());
        m.put("resolvedAt", t.getResolvedAt() == null ? null : t.getResolvedAt().toString());
        m.put("statusHistory", stackToJsonList(t.getStatusHistory()));
        return m;
    }

    private static void loadTickets() {
        try {
            String raw = JsonFileHandler.readFile(TICKETS_FILE);
            if (raw == null || raw.isBlank()) {
                return;
            }
            Map<String, Object> root = JsonFileHandler.parseObject(raw);
            CustomQueue<Ticket> pending = new CustomQueue<>();
            CustomArrayList<Ticket> resolved = new CustomArrayList<>();

            List<Map<String, Object>> pendingRows = getObjectList(root, "pending");
            List<Map<String, Object>> resolvedRows = getObjectList(root, "resolved");
            for (Map<String, Object> row : pendingRows) {
                Ticket t = ticketFromMap(row);
                if (t != null) {
                    pending.enqueue(t);
                }
            }
            for (Map<String, Object> row : resolvedRows) {
                Ticket t = ticketFromMap(row);
                if (t != null) {
                    resolved.add(t);
                }
            }

            int counter = getInt(root, "ticketCounter", 1000);
            HelpDeskService.getInstance().restoreTicketData(pending, resolved, counter);
        } catch (Exception e) {
            System.err.println("Failed to load tickets: " + e.getMessage());
        }
    }

    private static Ticket ticketFromMap(Map<String, Object> row) {
        String id = getStr(row, "id", null);
        String studentNumber = getStr(row, "studentNumber", null);
        String subject = getStr(row, "subject", null);
        String description = getStr(row, "description", null);
        if (id == null || studentNumber == null || subject == null || description == null) {
            return null;
        }

        LocalDateTime createdAt = null;
        String createdRaw = getStr(row, "createdAt", null);
        if (createdRaw != null && !createdRaw.isBlank()) {
            createdAt = LocalDateTime.parse(createdRaw);
        }

        LocalDateTime resolvedAt = null;
        String resolvedRaw = getStr(row, "resolvedAt", null);
        if (resolvedRaw != null && !resolvedRaw.isBlank() && !"null".equalsIgnoreCase(resolvedRaw)) {
            resolvedAt = LocalDateTime.parse(resolvedRaw);
        }

        CustomStack<String> history = jsonListToStack(row.get("statusHistory"));
        return new Ticket(
                id,
                studentNumber,
                subject,
                description,
                getStr(row, "status", Ticket.STATUS_OPEN),
                createdAt,
                resolvedAt,
                history
        );
    }

    // ============================================================
    //  EVENTS
    // ============================================================

    public static void saveEvents(CustomHashMap<String, Event> events) {
        try {
            List<Object> arr = new ArrayList<>();
            CustomArrayList<Event> vals = events.values();
            for (int i = 0; i < vals.size(); i++) {
                arr.add(eventToMap(vals.get(i)));
            }
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("events", arr);
            JsonFileHandler.writeFile(EVENTS_FILE, JsonFileHandler.stringifyObject(root));
        } catch (IOException e) {
            System.err.println("Failed to save events: " + e.getMessage());
        }
    }

    private static Map<String, Object> eventToMap(Event e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("eventId", e.getEventId());
        m.put("eventName", e.getEventName());
        m.put("description", e.getDescription() == null ? "" : e.getDescription());
        m.put("venue", e.getVenue());
        m.put("startDateTime", e.getStartDateTime() == null ? null : e.getStartDateTime().toString());
        m.put("endDateTime", e.getEndDateTime() == null ? null : e.getEndDateTime().toString());
        m.put("maxParticipants", e.getMaxParticipants());
        m.put("organizer", e.getOrganizer());
        m.put("registeredStudents", stringListToJson(e.getRegisteredStudents()));
        return m;
    }

    private static void loadEvents() {
        try {
            String raw = JsonFileHandler.readFile(EVENTS_FILE);
            if (raw == null || raw.isBlank()) {
                return;
            }
            Map<String, Object> root = JsonFileHandler.parseObject(raw);
            List<Map<String, Object>> rows = getObjectList(root, "events");
            CustomHashMap<String, Event> map = new CustomHashMap<>();
            for (Map<String, Object> row : rows) {
                Event e = eventFromMap(row);
                if (e != null) {
                    map.put(e.getEventId(), e);
                }
            }
            EventService.getInstance().setEvents(map);
        } catch (Exception e) {
            System.err.println("Failed to load events: " + e.getMessage());
        }
    }

    private static Event eventFromMap(Map<String, Object> row) {
        String id = getStr(row, "eventId", null);
        if (id == null) {
            return null;
        }
        LocalDateTime start = parseDateTimeSafe(getStr(row, "startDateTime", null));
        LocalDateTime end = parseDateTimeSafe(getStr(row, "endDateTime", null));
        if (start == null) {
            start = LocalDateTime.now();
        }
        if (end == null) {
            end = start.plusHours(1);
        }
        Event e = new Event(
                id,
                getStr(row, "eventName", ""),
                getStr(row, "description", ""),
                getStr(row, "venue", ""),
                start,
                end,
                getInt(row, "maxParticipants", 50),
                getStr(row, "organizer", "Campus")
        );
        // Restore the attendee list directly.
        e.setRegisteredStudents(readStringListObj(row.get("registeredStudents")));
        return e;
    }

    private static LocalDateTime parseDateTimeSafe(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text);
        } catch (Exception e) {
            return null;
        }
    }

    // ============================================================
    //  JSON CONVERSION HELPERS
    // ============================================================

    private static List<Object> stringListToJson(CustomArrayList<String> list) {
        List<Object> out = new ArrayList<>();
        if (list == null) {
            return out;
        }
        for (int i = 0; i < list.size(); i++) {
            out.add(list.get(i));
        }
        return out;
    }

    private static CustomArrayList<String> readStringListObj(Object o) {
        CustomArrayList<String> out = new CustomArrayList<>();
        if (!(o instanceof List)) {
            return out;
        }
        for (Object x : (List<?>) o) {
            if (x != null) {
                out.add(x.toString());
            }
        }
        return out;
    }

    private static List<Object> queueToJsonList(CustomQueue<String> queue) {
        List<Object> out = new ArrayList<>();
        if (queue == null || queue.isEmpty()) {
            return out;
        }
        // Walk the queue without losing its state.
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

    private static List<Object> stackToJsonList(CustomStack<String> stack) {
        List<Object> out = new ArrayList<>();
        if (stack == null || stack.isEmpty()) {
            return out;
        }
        // Pop into a temp stack, then push back to keep original order.
        CustomStack<String> temp = new CustomStack<>();
        while (!stack.isEmpty()) {
            temp.push(stack.pop());
        }
        while (!temp.isEmpty()) {
            String item = temp.pop();
            out.add(item);
            stack.push(item);
        }
        return out;
    }

    private static CustomStack<String> jsonListToStack(Object raw) {
        CustomStack<String> out = new CustomStack<>();
        if (!(raw instanceof List)) {
            return out;
        }
        for (Object item : (List<?>) raw) {
            if (item != null) {
                out.push(item.toString());
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getObjectList(Map<String, Object> root, String key) {
        Object o = root.get(key);
        List<Map<String, Object>> out = new ArrayList<>();
        if (!(o instanceof List)) {
            return out;
        }
        for (Object x : (List<?>) o) {
            if (x instanceof Map) {
                out.add((Map<String, Object>) x);
            }
        }
        return out;
    }

    private static String getStr(Map<String, Object> m, String k, String def) {
        Object o = m.get(k);
        if (o == null) {
            return def;
        }
        return o.toString();
    }

    private static int getInt(Map<String, Object> m, String k, int def) {
        Object o = m.get(k);
        if (o == null) {
            return def;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    // ============================================================
    //  SCHEMA VALIDATION (best effort, prints warnings)
    // ============================================================

    private static void validateConfiguredDataFiles() {
        validateFileAgainstSchema(STUDENTS_FILE, STUDENTS_SCHEMA_FILE);
        validateFileAgainstSchema(COURSES_FILE, COURSES_SCHEMA_FILE);
        validateFileAgainstSchema(BOOKS_FILE, BOOKS_SCHEMA_FILE);
        validateFileAgainstSchema(HOSTELS_FILE, HOSTELS_SCHEMA_FILE);
        validateFileAgainstSchema(TICKETS_FILE, TICKETS_SCHEMA_FILE);
        validateFileAgainstSchema(EVENTS_FILE, EVENTS_SCHEMA_FILE);
    }

    private static void validateFileAgainstSchema(String dataFile, String schemaFile) {
        try {
            String dataRaw = JsonFileHandler.readFile(dataFile);
            String schemaRaw = JsonFileHandler.readFile(schemaFile);

            if (schemaRaw == null || schemaRaw.isBlank()) {
                handleValidationIssue("Schema missing/empty: " + schemaFile, null);
                return;
            }
            if (dataRaw == null || dataRaw.isBlank()) {
                handleValidationIssue("Data file missing/empty: " + dataFile, null);
                return;
            }

            Map<String, Object> data = JsonFileHandler.parseObject(dataRaw);
            Map<String, Object> schema = JsonFileHandler.parseObject(schemaRaw);
            JsonSchemaValidator.validate(data, schema, dataFile);
        } catch (Exception e) {
            handleValidationIssue("Schema validation failed for " + dataFile + ": " + e.getMessage(), e);
        }
    }

    private static void handleValidationIssue(String message, Exception e) {
        if (STRICT_SCHEMA_VALIDATION) {
            if (e == null) {
                throw new IllegalStateException(message);
            }
            throw new IllegalStateException(message, e);
        }
        System.err.println("[schema-warning] " + message);
    }
}
