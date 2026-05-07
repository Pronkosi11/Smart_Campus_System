package service;

import model.Student;
import datastructures.CustomHashMap;
import datastructures.CustomArrayList;
import persistence.DataPersistence;
import ui.BoxUI;

/**
 * StudentService - Student Management Service
 * 
 * This service class handles all student-related operations in the Smart Campus System.
 * It provides comprehensive student management functionality including:
 * 
 * Core Operations:
 * - Student registration and profile management
 * - CRUD operations (Create, Read, Update, Delete)
 * - Student lookup and filtering by faculty
 * - Integration with authentication system
 * 
 * UI Integration:
 * - Admin interface for student management
 * - Student profile viewing
 * - Clean BoxUI-based console interface
 * 
 * Data Management:
 * - Uses CustomHashMap for efficient student storage (O(1) lookup by student number)
 * - Automatic persistence through DataPersistence layer
 * - Integration with LoginService for authentication
 * 
 * This class follows the Singleton pattern to ensure centralized student data management
 * across the entire application.
 */
public class StudentService {

    // Primary storage for all students - indexed by student number for fast lookup
    private CustomHashMap<String, Student> students;

    // Singleton instance to ensure centralized student management
    private static StudentService instance;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the student storage HashMap.
     */
    private StudentService() {
        students = new CustomHashMap<>();
    }

    /**
     * Returns the singleton instance of StudentService.
     * Creates the instance if it doesn't exist (lazy initialization).
     * 
     * @return The single StudentService instance
     */
    public static StudentService getInstance() {
        if (instance == null) {
            instance = new StudentService();
        }
        return instance;
    }

    /**
     * Adds a new student to the system with full integration.
     * 
     * This method:
     * - Stores the student in the internal HashMap for fast lookup
     * - Registers the student with LoginService for authentication
     * - Persists the updated student data to JSON storage
     * 
     * @param student The Student object to add to the system
     */
    public void addStudent(Student student) {
        students.put(student.getStudentNumber(), student);
        LoginService.getInstance().registerStudent(student);
        DataPersistence.saveStudents(students);
    }

    /**
     * Retrieves a student by their student number.
     * 
     * @param studentNumber The unique student identifier
     * @return The Student object if found, null otherwise
     */
    public Student getStudent(String studentNumber) {
        return students.get(studentNumber);
    }

    /**
     * Updates an existing student's information.
     * 
     * This method replaces the existing student record with the updated version
     * and persists the changes to storage.
     * 
     * @param student The updated Student object
     */
    public void updateStudent(Student student) {
        students.put(student.getStudentNumber(), student);
        DataPersistence.saveStudents(students);
    }

    /**
     * Removes a student from the system completely.
     * 
     * This method:
     * - Removes the student from internal storage
     * - Unregisters them from the authentication system
     * - Persists the changes to storage
     * 
     * @param studentNumber The student number of the student to delete
     * @return true if student was found and deleted, false otherwise
     */
    public boolean deleteStudent(String studentNumber) {
        Student removed = students.remove(studentNumber);
        if (removed != null) {
            LoginService.getInstance().unregisterStudent(studentNumber);
            DataPersistence.saveStudents(students);
            return true;
        }
        return false;
    }

    /**
     * Returns all students in the system as a list.
     * 
     * @return CustomArrayList containing all Student objects
     */
    public CustomArrayList<Student> getAllStudents() {
        return students.values();
    }

    /**
     * Filters students by faculty (case-insensitive).
     * 
     * This method searches through all students and returns those
     * whose faculty matches the specified faculty name.
     * 
     * @param faculty The faculty name to filter by
     * @return CustomArrayList of students in the specified faculty
     */
    public CustomArrayList<Student> getStudentsByFaculty(String faculty) {
        CustomArrayList<Student> result = new CustomArrayList<>();
        CustomArrayList<Student> allStudents = students.values();

        for (Student student : allStudents) {
            if (student.getFaculty().equalsIgnoreCase(faculty)) {
                result.add(student);
            }
        }
        return result;
    }

    /**
     * Sets the entire student collection (used during data loading from persistence).
     * 
     * @param loadedStudents The HashMap of students loaded from storage
     */
    public void setStudents(CustomHashMap<String, Student> loadedStudents) {
        this.students = loadedStudents;
    }

    /**
     * Returns the internal student storage HashMap (used by persistence layer).
     * 
     * @return The internal CustomHashMap containing all students
     */
    public CustomHashMap<String, Student> getStudentsMap() {
        return students;
    }

    // ========== UI-FACING MODULE FLOW METHODS ==========
    // These methods provide simplified interfaces for UI components

    /**
     * Checks if a student exists in the system.
     * 
     * @param studentNumber The student number to check
     * @return true if student exists, false otherwise
     */
    public boolean studentExists(String studentNumber) {
        return getStudent(studentNumber) != null;
    }

    /**
     * Registers a new student (alias for addStudent for UI clarity).
     * 
     * @param student The Student object to register
     */
    public void registerStudent(Student student) {
        addStudent(student);
    }

    /**
     * Gets a student by number (alias for getStudent for UI clarity).
     * 
     * @param studentNumber The student number to look up
     * @return The Student object if found, null otherwise
     */
    public Student getStudentByNumber(String studentNumber) {
        return getStudent(studentNumber);
    }

    /**
     * Deletes a student by number (alias for deleteStudent for UI clarity).
     * 
     * @param studentNumber The student number to delete
     * @return true if student was deleted, false if not found
     */
    public boolean deleteStudentByNumber(String studentNumber) {
        return deleteStudent(studentNumber);
    }

    /**
     * Displays the admin student management menu and handles user interactions.
     * 
     * This method provides a complete admin interface for student management including:
     * - Listing all students
     * - Viewing individual student details
     * - Deleting students
     * - Filtering students by faculty
     * 
     * The menu loop continues until the admin chooses to go back.
     * 
     * @param box The BoxUI instance for clean console interface
     */
    public void showAdminStudentsMenu(BoxUI box) {
        int option;
        do {
            box.printMenu("MANAGE STUDENTS", new String[]{
                    "1. List All Students",
                    "2. View Student by Number",
                    "3. Delete Student",
                    "4. List by Faculty",
                    "5. Back"
            });

            option = box.readInt("Choose option: ", 1, 5);
            switch (option) {
                case 1:
                    listAllStudents(box);
                    break;
                case 2:
                    viewStudentByNumber(box);
                    break;
                case 3:
                    deleteStudentByNumberUI(box);
                    break;
                case 4:
                    listByFaculty(box);
                    break;
                case 5:
                    break;
                default:
                    box.error("Invalid option.");
            }
        } while (option != 5);
    }

    /**
     * Displays a student's profile information in a formatted section.
     * 
     * This method shows the student's complete profile including personal
     * information, academic details, and enrollment statistics.
     * 
     * @param box The BoxUI instance for clean console interface
     * @param student The Student object whose profile to display
     */
    public void showStudentProfile(BoxUI box, Student student) {
        box.printSection("MY PROFILE", 
            "Student No.: " + student.getStudentNumber(),
            "Name       : " + student.getName(),
            "Gender     : " + student.getGender(),
            "Faculty : " + student.getFaculty(),
            "Year       : " + student.getYear(),
            "Email      : " + student.getEmail(),
            "Phone      : " + student.getPhone(),
            "Courses    : " + student.getEnrolledCourses().size());
        printStudentDetails(box, student);
        box.endSection();
    }

    // ========== PRIVATE HELPER METHODS ==========
    // These methods handle specific UI flows and data formatting

    /**
     * Lists all students in the system with formatted display.
     * 
     * This method retrieves all students and displays them in a numbered list
     * format. If no students exist, it shows an appropriate message.
     * 
     * @param box The BoxUI instance for clean console interface
     */
    private void listAllStudents(BoxUI box) {
        CustomArrayList<Student> all = getAllStudents();
        if (all.isEmpty()) {
            box.info("No students found.");
            return;
        }

        String[] lines = new String[all.size()];
        for (int i = 0; i < all.size(); i++) {
            lines[i] = (i + 1) + ". " + all.get(i);
        }
        box.printSection("ALL STUDENTS", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    /**
     * Prompts for a student number and displays their detailed information.
     * 
     * This method handles the admin flow for viewing a specific student's
     * complete profile information. It includes error handling for invalid
     * or non-existent student numbers.
     * 
     * @param box The BoxUI instance for clean console interface
     */
    private void viewStudentByNumber(BoxUI box) {
        String studentNumber = box.prompt("Enter student number: ");
        Student s = getStudentByNumber(studentNumber);
        if (s == null) {
            box.error("Student not found.");
            return;
        }
        box.printSection("STUDENT DETAILS", 
            "Student No.: " + s.getStudentNumber(),
            "Name       : " + s.getName(),
            "Gender     : " + s.getGender(),
            "Faculty : " + s.getFaculty(),
            "Year       : " + s.getYear(),
            "Email      : " + s.getEmail(),
            "Phone      : " + s.getPhone(),
            "Courses    : " + s.getEnrolledCourses().size());
        printStudentDetails(box, s);
        box.endSection();
    }

    /**
     * Prompts for a student number and deletes the student from the system.
     * 
     * This method handles the admin flow for student deletion with appropriate
     * success/error feedback. The deletion includes removal from authentication
     * and persistence layers.
     * 
     * @param box The BoxUI instance for clean console interface
     */
    private void deleteStudentByNumberUI(BoxUI box) {
        String deleteNumber = box.prompt("Enter student number to delete: ");
        if (deleteStudentByNumber(deleteNumber)) {
            box.success("Student deleted.");
        } else {
            box.error("Student not found.");
        }
    }

    /**
     * Prompts for a faculty name and lists all students in that faculty.
     * 
     * This method provides filtered student listing by faculty with
     * case-insensitive matching. Results are displayed in a numbered list
     * format within a formatted section.
     * 
     * @param box The BoxUI instance for clean console interface
     */
    private void listByFaculty(BoxUI box) {
        String faculty = box.prompt("Enter faculty: ");
        CustomArrayList<Student> byFaculty = getStudentsByFaculty(faculty);
        if (byFaculty.isEmpty()) {
            box.info("No students found in this faculty.");
            return;
        }
        String[] lines = new String[byFaculty.size()];
        for (int i = 0; i < byFaculty.size(); i++) {
            lines[i] = (i + 1) + ". " + byFaculty.get(i);
        }
        box.printSection("STUDENTS BY FACULTY", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    /**
     * Formats and displays detailed student information in a consistent layout.
     * 
     * This method provides a standardized way to display student details
     * including personal information, academic details, and enrollment statistics.
     * Used by both admin and student profile views.
     * 
     * @param box The BoxUI instance for clean console interface
     * @param student The Student object whose details to display
     */
    private void printStudentDetails(BoxUI box, Student student) {
        box.line("Student No.: " + student.getStudentNumber());
        box.line("Name       : " + student.getName());
        box.line("Gender     : " + student.getGender());
        box.line("Faculty : " + student.getFaculty());
        box.line("Year       : " + student.getYear());
        box.line("Email      : " + student.getEmail());
        box.line("Phone      : " + student.getPhone());
        box.line("Courses    : " + student.getEnrolledCourses().size());
    }
}
