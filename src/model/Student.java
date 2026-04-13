package model;

import datastructures.CustomArrayList;
import java.time.LocalDate;

public class Student extends User {
    private String gender;
    private String department;
    private int year;
    private String email;
    private String phone;
    private CustomArrayList<String> enrolledCourses;
    private CustomArrayList<String> borrowedBooks;
    private String hostelRoom;
    private LocalDate registrationDate;

    public Student(String studentNumber, String name, String password, String gender, String department,
                   int year, String email, String phone) {
        super(studentNumber, name, password, "STUDENT");
        this.gender = gender;
        this.department = department;
        this.year = year;
        this.email = email;
        this.phone = phone;
        this.enrolledCourses = new CustomArrayList<>();
        this.borrowedBooks = new CustomArrayList<>();
        this.hostelRoom = null;
        this.registrationDate = LocalDate.now();
    }

    // Getters and Setters
    public String getStudentNumber() { return getId(); }
    public void setStudentNumber(String studentNumber) { setId(studentNumber); }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

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

    public void enrollCourse(String courseCode) {
        if (!enrolledCourses.contains(courseCode)) {
            enrolledCourses.add(courseCode);
        }
    }

    public void dropCourse(String courseCode) {
        enrolledCourses.remove(courseCode);
    }

    public void borrowBook(String bookId) {
        if (!borrowedBooks.contains(bookId)) {
            borrowedBooks.add(bookId);
        }
    }

    public void returnBook(String bookId) {
        borrowedBooks.remove(bookId);
    }

    @Override
    public String toString() {
        return String.format("Student [Student Number: %s, Name: %s, Gender: %s, Dept: %s, Year: %d, Courses: %d]",
                getStudentNumber(), name, gender, department, year, enrolledCourses.size());
    }
}