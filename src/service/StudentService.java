package service;

import java.util.ArrayList;
import model.Student;
import persistance.StudentFileHandler;

public class StudentService {

    private ArrayList<Student> students;

    public StudentService() {
        students = StudentFileHandler.loadStudents();
    }

    // 🔍 Find student by number
    public Student findStudent(String studentNumber) {
        for (Student s : students) {
            if (s.getStudentNumber().equals(studentNumber)) {
                return s;
            }
        }
        return null;
    }

    // 🔐 Login logic
    public Student login(String studentNumber, String password) {
        Student student = findStudent(studentNumber);

        if (student != null && student.getPassword().equals(password)) {
            return student;
        }

        return null;
    }
}