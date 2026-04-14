package service;

import model.Student;
import datastructures.CustomHashMap;
import datastructures.CustomArrayList;
import persistence.DataPersistence;
import ui.BoxUI;

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

    // UI-facing module flow methods
    public boolean studentExists(String studentNumber) {
        return getStudent(studentNumber) != null;
    }

    public void registerStudent(Student student) {
        addStudent(student);
    }

    public Student getStudentByNumber(String studentNumber) {
        return getStudent(studentNumber);
    }

    public boolean deleteStudentByNumber(String studentNumber) {
        return deleteStudent(studentNumber);
    }

    public void showAdminStudentsMenu(BoxUI box) {
        int option;
        do {
            box.printMenu("MANAGE STUDENTS", new String[]{
                    "1. List All Students",
                    "2. View Student by Number",
                    "3. Delete Student",
                    "4. List by Department",
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
                    deleteStudentByNumber(box);
                    break;
                case 4:
                    listByDepartment(box);
                    break;
                case 5:
                    break;
                default:
                    box.error("Invalid option.");
            }
        } while (option != 5);
    }

    public void showStudentProfile(BoxUI box, Student student) {
        box.printSection("MY PROFILE");
        printStudentDetails(box, student);
        box.endSection();
    }

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

    private void viewStudentByNumber(BoxUI box) {
        String studentNumber = box.prompt("Enter student number: ");
        Student s = getStudentByNumber(studentNumber);
        if (s == null) {
            box.error("Student not found.");
            return;
        }
        box.printSection("STUDENT DETAILS");
        printStudentDetails(box, s);
        box.endSection();
    }

    private void deleteStudentByNumber(BoxUI box) {
        String deleteNumber = box.prompt("Enter student number to delete: ");
        if (deleteStudentByNumber(deleteNumber)) {
            box.success("Student deleted.");
        } else {
            box.error("Student not found.");
        }
    }

    private void listByDepartment(BoxUI box) {
        String department = box.prompt("Enter department: ");
        CustomArrayList<Student> byDept = getStudentsByDepartment(department);
        if (byDept.isEmpty()) {
            box.info("No students found in this department.");
            return;
        }
        String[] lines = new String[byDept.size()];
        for (int i = 0; i < byDept.size(); i++) {
            lines[i] = (i + 1) + ". " + byDept.get(i);
        }
        box.printSection("STUDENTS BY DEPARTMENT", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    private void printStudentDetails(BoxUI box, Student student) {
        box.line("Student No.: " + student.getStudentNumber());
        box.line("Name       : " + student.getName());
        box.line("Gender     : " + student.getGender());
        box.line("Department : " + student.getDepartment());
        box.line("Year       : " + student.getYear());
        box.line("Email      : " + student.getEmail());
        box.line("Phone      : " + student.getPhone());
        box.line("Courses    : " + student.getEnrolledCourses().size());
    }
}