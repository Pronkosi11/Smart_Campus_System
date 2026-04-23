package model;

import datastructures.CustomArrayList;

/**
 * Course - Course Entity Model
 * 
 * This class represents a course in the Smart Campus Management System.
 * It manages course information, enrollment tracking, and capacity management.
 * 
 * Key Features:
 * - Course information management (code, name, instructor, credits, schedule)
 * - Enrollment capacity tracking and enforcement
 * - Student enrollment and dropping functionality
 * - Persistence support for loading/saving course data
 * 
 * The class provides methods for:
 * - Student enrollment with capacity checking
 * - Student dropping with count management
 * - Capacity and availability checking
 * - Persistence data loading
 * - String representation for display purposes
 * 
 * This model integrates with CourseService for business logic and
 * StudentService for cross-referencing student enrollments.
 */
public class Course {

    // ========== COURSE INFORMATION ==========
    private String courseCode;      // Unique course identifier (e.g., "CS101", "MATH201")
    private String courseName;      // Full course name (e.g., "Introduction to Computer Science")
    private String instructor;      // Name of the course instructor/professor
    private int credits;           // Number of academic credits for this course
    private String schedule;       // Course schedule information (e.g., "MWF 10:00-11:00")

    // ========== ENROLLMENT MANAGEMENT ==========
    private int maxCapacity;                           // Maximum number of students that can enroll
    private int enrolledCount;                         // Current number of enrolled students
    private CustomArrayList<String> enrolledStudents; // List of student numbers enrolled in this course

    /**
     * Constructs a new Course with the provided information.
     * 
     * This constructor initializes a course with all required academic information.
     * It automatically sets up an empty enrollment list and sets the enrolled count to 0.
     * 
     * @param courseCode Unique course identifier (e.g., "CS101", "MATH201")
     * @param courseName Full descriptive name of the course
     * @param instructor Name of the instructor teaching this course
     * @param credits Number of academic credits this course is worth
     * @param maxCapacity Maximum number of students that can enroll
     * @param schedule Course meeting schedule (e.g., "MWF 10:00-11:00")
     */
    public Course(String courseCode, String courseName, String instructor,
                  int credits, int maxCapacity, String schedule) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.instructor = instructor;
        this.credits = credits;
        this.maxCapacity = maxCapacity;
        this.enrolledCount = 0;  // Start with no enrolled students
        this.enrolledStudents = new CustomArrayList<>();  // Initialize empty enrollment list
        this.schedule = schedule;
    }

    // ========== GETTERS AND SETTERS ==========
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public int getEnrolledCount() { return enrolledCount; }

    public CustomArrayList<String> getEnrolledStudents() { return enrolledStudents; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    // ========== ENROLLMENT MANAGEMENT METHODS ==========

    /**
     * Checks if the course has reached its maximum enrollment capacity.
     * 
     * @return true if the course is at maximum capacity, false otherwise
     */
    public boolean isFull() {
        return enrolledCount >= maxCapacity;
    }

    /**
     * Attempts to enroll a student in the course.
     * 
     * This method checks both capacity and duplicate enrollment before adding
     * a student to the course. It maintains the enrolled count automatically.
     * 
     * @param studentNumber The student number of the student to enroll
     * @return true if enrollment was successful, false if course is full or student already enrolled
     */
    public boolean enrollStudent(String studentNumber) {
        if (!isFull() && !enrolledStudents.contains(studentNumber)) {
            enrolledStudents.add(studentNumber);
            enrolledCount++;
            return true;
        }
        return false;
    }

    /**
     * Attempts to drop a student from the course.
     * 
     * This method removes a student from the enrollment list and decrements
     * the enrolled count if the student was found in the course.
     * 
     * @param studentNumber The student number of the student to drop
     * @return true if the student was successfully dropped, false if not enrolled
     */
    public boolean dropStudent(String studentNumber) {
        if (enrolledStudents.remove(studentNumber)) {
            enrolledCount--;
            return true;
        }
        return false;
    }

    /**
     * Restores enrollment data from persistence (used during data loading).
     * 
     * This method is used by the persistence layer to restore course enrollment
     * data from JSON files. It bypasses capacity checks since the data is
     * assumed to be valid from a previous session.
     * 
     * @param ids List of student numbers to restore as enrolled students
     */
    public void loadEnrolledFromPersistence(CustomArrayList<String> ids) {
        enrolledStudents.clear();
        enrolledCount = 0;
        if (ids == null) {
            return;
        }
        for (int i = 0; i < ids.size(); i++) {
            enrolledStudents.add(ids.get(i));
            enrolledCount++;
        }
    }

    /**
     * Returns a formatted string representation of the course for display purposes.
     * 
     * This method provides a concise summary of the course including the course code,
     * name, instructor, and current enrollment status with capacity information.
     * Used in course listings and selection menus throughout the UI.
     * 
     * @return A formatted string containing key course information and enrollment status
     */
    @Override
    public String toString() {
        return String.format("%s - %s (%s) [%d/%d enrolled]",
                courseCode, courseName, instructor, enrolledCount, maxCapacity);
    }
}
