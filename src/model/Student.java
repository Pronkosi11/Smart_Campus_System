package model;

public class Student {
    private String studentNumber;
    private String name;
    private String surname;
    private String password;

    public Student(String studentNumber, String name, String surname, String password) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.surname = surname;
        this.password = password;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getPassword() {
        return password;
    }

    public String toFileString() {
        return studentNumber + "|" + name + "|" + surname + "|" + password;
    }
}