package model;

import datastructures.CustomArrayList;
import java.time.LocalDate;

/**
 * Student - Student Entity Model
 * 
 * This class represents a student in the Smart Campus Management System.
 * It extends the base User class and adds student-specific attributes and functionality.
 * 
 * Key Features:
 * - Personal information management (demographics, contact details)
 * - Academic information (faculty, year of study)
 * - Course enrollment tracking
 * - Library book borrowing management
 * - Hostel room assignment
 * - Registration date tracking
 * 
 * The class provides methods for:
 * - Course enrollment and dropping
 * - Book borrowing and returning
 * - Profile information access
 * - String representation for display purposes
 * 
 * This model integrates with various services including StudentService,
 * CourseService, LibraryService, and HostelService.
 */
public class Student extends User {

    // ========== PERSONAL INFORMATION ==========
    private String gender;          // Student's gender (Male/Female/Other/Unspecified)
    private String faculty;      // Academic faculty (e.g., Computer Science, Engineering)
    private int year;              // Year of study (1-4)
    private String email;          // Contact email address
    private String phone;          // Contact phone number

    // ========== ACADEMIC & SERVICE TRACKING ==========
    private CustomArrayList<String> enrolledCourses;  // List of course codes student is enrolled in
    private CustomArrayList<String> borrowedBooks;    // List of book IDs currently borrowed from library
    private String hostelRoom;                         // Assigned hostel room (null if not assigned)
    private LocalDate registrationDate;               // Date when student registered in the system

    /**
     * Constructs a new Student with the provided information.
     *
     * This constructor initializes a student with all required personal and academic
     * information. It automatically sets up empty collections for courses and books,
     * sets the registration date to the current date, and leaves hostel room unassigned.
     *
     * @param studentNumber Unique student identifier (must be a valid integer, used as login username)
     * @param name Full name of the student
     * @param password Login password for authentication
     * @param gender Student's gender (Male/Female/Other/Unspecified)
     * @param faculty Academic faculty the student belongs to
     * @param year Year of study (typically 1-4)
     * @param email Contact email address
     * @param phone Contact phone number
     * @throws IllegalArgumentException if studentNumber is not a valid integer
     */
    public Student(String studentNumber, String name, String password, String gender, String faculty,
                   int year, String email, String phone) {
        super(studentNumber, name, password, "STUDENT");
        validateStudentNumber(studentNumber);
        validateYear(year);
        this.gender = gender;
        this.faculty = faculty;
        this.year = year;
        this.email = email;
        this.phone = phone;
        this.enrolledCourses = new CustomArrayList<>();
        this.borrowedBooks = new CustomArrayList<>();
        this.hostelRoom = null;  // Initially no room assigned
        this.registrationDate = LocalDate.now();  // Set to current date
    }

    // ========== GETTERS AND SETTERS ==========
    public String getStudentNumber() { return getId(); }
    public void setStudentNumber(String studentNumber) {
        validateStudentNumber(studentNumber);
        setId(studentNumber);
    }

    /**
     * Validates that the student number is a valid integer.
     *
     * @param studentNumber The student number to validate
     * @throws IllegalArgumentException if studentNumber is null, empty, or not a valid integer
     */
    private void validateStudentNumber(String studentNumber) {
        if (studentNumber == null || studentNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Student number cannot be null or empty");
        }
        try {
            Long.parseLong(studentNumber.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Student number must be a valid integer: " + studentNumber);
        }
    }

    /**
     * Validates that the year of study is within the valid range (1-4).
     *
     * @param year The year of study to validate
     * @throws IllegalArgumentException if year is not between 1 and 4
     */
    private void validateYear(int year) {
        if (year < 1 || year > 4) {
            throw new IllegalArgumentException("Year of study must be between 1 and 4: " + year);
        }
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public int getYear() { return year; }
    public void setYear(int year) {
        validateYear(year);
        this.year = year;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public CustomArrayList<String> getEnrolledCourses() { return enrolledCourses; }

    public CustomArrayList<String> getBorrowedBooks() { return borrowedBooks; }

    public String getHostelRoom() { return hostelRoom; }
    public void setHostelRoom(String hostelRoom) { this.hostelRoom = hostelRoom; }

    public LocalDate getRegistrationDate() { return registrationDate; }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate != null ? registrationDate : LocalDate.now();
    }

    // ========== COURSE MANAGEMENT METHODS ==========

    /**
     * Enrolls the student in a course if not already enrolled.
     * 
     * This method adds a course code to the student's enrolled courses list,
     * but only if the student is not already enrolled in that course.
     * Prevents duplicate enrollments.
     * 
     * @param courseCode The course code to enroll in (e.g., "CS101", "MATH201")
     */
    public void enrollCourse(String courseCode) {
        if (!enrolledCourses.contains(courseCode)) {
            enrolledCourses.add(courseCode);
        }
    }

    /**
     * Drops the student from a course.
     * 
     * This method removes a course code from the student's enrolled courses list.
     * If the student is not enrolled in the course, no action is taken.
     * 
     * @param courseCode The course code to drop from
     */
    public void dropCourse(String courseCode) {
        enrolledCourses.remove(courseCode);
    }

    // ========== LIBRARY MANAGEMENT METHODS ==========

    /**
     * Records that the student has borrowed a book from the library.
     * 
     * This method adds a book ID to the student's borrowed books list,
     * but only if the book is not already recorded as borrowed.
     * Prevents duplicate borrowing records.
     * 
     * @param bookId The unique identifier of the borrowed book
     */
    public void borrowBook(String bookId) {
        if (!borrowedBooks.contains(bookId)) {
            borrowedBooks.add(bookId);
        }
    }

    /**
     * Records that the student has returned a book to the library.
     * 
     * This method removes a book ID from the student's borrowed books list.
     * If the book is not in the borrowed list, no action is taken.
     * 
     * @param bookId The unique identifier of the returned book
     */
    public void returnBook(String bookId) {
        borrowedBooks.remove(bookId);
    }

    /**
     * Returns a formatted string representation of the student for display purposes.
     * 
     * This method provides a concise summary of the student including their
     * student number, name, gender, faculty, year, and number of enrolled courses.
     * Used in lists and summary displays throughout the UI.
     * 
     * @return A formatted string containing key student information
     */
    @Override
    public String toString() {
        return String.format("Student [Student Number: %s, Name: %s, Gender: %s, Faculty: %s, Year: %d, Courses: %d]",
                getStudentNumber(), name, gender, faculty, year, enrolledCourses.size());
    }
}
