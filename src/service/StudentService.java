package service;

import model.Student;
import datastructures.CustomHashMap;
import datastructures.CustomArrayList;
import persistence.DataPersistence;

public class StudentService {
    private CustomHashMap<String, Student> students;
    private static StudentService instance;

    private StudentService() {
        students = new CustomHashMap<>();
    }

    public static StudentService getInstance() {
        if (instance == null) {
            instance = new StudentService();
        }
        return instance;
    }

    public void addStudent(Student student) {
        students.put(student.getStudentNumber(), student);
        LoginService.getInstance().registerStudent(student);
        DataPersistence.saveStudents(students);
    }

    public Student getStudent(String studentNumber) {
        return students.get(studentNumber);
    }

    public void updateStudent(Student student) {
        students.put(student.getStudentNumber(), student);
        DataPersistence.saveStudents(students);
    }

    public boolean deleteStudent(String studentNumber) {
        Student removed = students.remove(studentNumber);
        if (removed != null) {
            LoginService.getInstance().unregisterStudent(studentNumber);
            DataPersistence.saveStudents(students);
            return true;
        }
        return false;
    }

    public CustomArrayList<Student> getAllStudents() {
        return students.values();
    }

    public CustomArrayList<Student> getStudentsByDepartment(String department) {
        CustomArrayList<Student> result = new CustomArrayList<>();
        CustomArrayList<Student> allStudents = students.values();

        for (Student student : allStudents) {
            if (student.getDepartment().equalsIgnoreCase(department)) {
                result.add(student);
            }
        }
        return result;
    }

    public void setStudents(CustomHashMap<String, Student> loadedStudents) {
        this.students = loadedStudents;
    }

    public CustomHashMap<String, Student> getStudentsMap() {
        return students;
    }
}