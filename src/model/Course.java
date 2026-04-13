package model;

import datastructures.CustomArrayList;

public class Course {
    private String courseCode;
    private String courseName;
    private String instructor;
    private int credits;
    private int maxCapacity;
    private int enrolledCount;
    private CustomArrayList<String> enrolledStudents;
    private String schedule;

    public Course(String courseCode, String courseName, String instructor,
                  int credits, int maxCapacity, String schedule) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.instructor = instructor;
        this.credits = credits;
        this.maxCapacity = maxCapacity;
        this.enrolledCount = 0;
        this.enrolledStudents = new CustomArrayList<>();
        this.schedule = schedule;
    }

    // Getters and Setters
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

    public boolean isFull() {
        return enrolledCount >= maxCapacity;
    }

    public boolean enrollStudent(String studentNumber) {
        if (!isFull() && !enrolledStudents.contains(studentNumber)) {
            enrolledStudents.add(studentNumber);
            enrolledCount++;
            return true;
        }
        return false;
    }

    public boolean dropStudent(String studentNumber) {
        if (enrolledStudents.remove(studentNumber)) {
            enrolledCount--;
            return true;
        }
        return false;
    }

    /** Restores enrollment from JSON (does not re-check capacity). */
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

    @Override
    public String toString() {
        return String.format("%s - %s (%s) [%d/%d enrolled]",
                courseCode, courseName, instructor, enrolledCount, maxCapacity);
    }
}