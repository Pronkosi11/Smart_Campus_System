package persistence;

import datastructures.CustomArrayList;
import datastructures.CustomHashMap;
import datastructures.CustomQueue;
import model.*;
import service.CourseService;

import service.LoginService;
import service.StudentService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loads and saves all domain data as JSON under {@code data/}.
 */
public final class DataPersistence {

    public static final String STUDENTS_FILE = "data/students.json";
    public static final String COURSES_FILE = "data/courses.json";
    // public static final String BOOKS_FILE = "data/books.json";
    // public static final String HOSTELS_FILE = "data/hostels.json";
    // public static final String TICKETS_FILE = "data/tickets.json";
    // public static final String EVENTS_FILE = "data/events.json";

    private DataPersistence() {
    }

    public static void loadAllData() {
        loadStudents();
        loadCourses();
    }

    public static void saveAllData() {
        saveStudents(StudentService.getInstance().getStudentsMap());
        saveCourses(CourseService.getInstance().getCoursesMap());
    }

    // --- Students ---

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
        m.put("id", s.getId());
        m.put("name", s.getName());
        m.put("password", s.getPassword());
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
                    map.put(s.getId(), s);
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
        String id = getStr(row, "id", null);
        if (id == null) {
            return null;
        }
        Student s = new Student(
                id,
                getStr(row, "name", ""),
                getStr(row, "password", ""),
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

    private static void clearAllStudentHostelRooms() {
        CustomArrayList<Student> all = StudentService.getInstance().getAllStudents();
        for (int i = 0; i < all.size(); i++) {
            all.get(i).setHostelRoom(null);
        }
    }


    // --- Courses ---

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

    // --- Books ---


    private static CustomArrayList<String> snapshotQueueIds(CustomQueue<String> q) {
        CustomArrayList<String> copy = new CustomArrayList<>();
        CustomQueue<String> temp = new CustomQueue<>();
        while (!q.isEmpty()) {
            String id = q.dequeue();
            copy.add(id);
            temp.enqueue(id);
        }
        while (!temp.isEmpty()) {
            q.enqueue(temp.dequeue());
        }
        return copy;
    }



    // --- Hostels + applications ---


    // --- Tickets ---



    // --- Events ---



    // --- JSON helpers ---

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
        return Integer.parseInt(o.toString());
    }

    private static double getDouble(Map<String, Object> m, String k, double def) {
        Object o = m.get(k);
        if (o == null) {
            return def;
        }
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return Double.parseDouble(o.toString());
    }
}
