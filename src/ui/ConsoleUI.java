package ui;

import model.*;
import datastructures.CustomArrayList;
import service.*;

import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Menu-driven console interface for admin and student roles.
 */
public class ConsoleUI {

    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        int choice;
        do {
            printMainMenu();
            choice = readInt("Choose option: ", 1, 4);
            switch (choice) {
                case 1:
                    handleLoginAsAdmin();
                    break;
                case 2:
                    handleLoginAsStudent();
                    break;
                case 3:
                    registerStudent();
                    break;
                case 4:
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 4);
    }

    private void printMainMenu() {
        System.out.println("\n===== SMART CAMPUS SYSTEM =====");
        System.out.println("1. Login as Admin");
        System.out.println("2. Login as Student");
        System.out.println("3. Register new student");
        System.out.println("4. Exit");
    }

    private void handleLoginAsAdmin() {
        System.out.print("Username: ");
        String user = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        User u = LoginService.getInstance().login(user, pass);
        if (u instanceof Admin) {
            System.out.println("Admin login successful.");
            adminMenu();
        } else {
            System.out.println("Invalid admin credentials.");
        }
    }

    private void handleLoginAsStudent() {
        System.out.print("Student ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        User u = LoginService.getInstance().login(id, pass);
        if (u instanceof Student) {
            System.out.println("Login successful.");
            studentMenu((Student) u);
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    private void registerStudent() {
        System.out.print("Student ID: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("ID cannot be empty.");
            return;
        }
        if (StudentService.getInstance().getStudent(id) != null) {
            System.out.println("Student already exists.");
            return;
        }
        System.out.print("Full name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Department: ");
        String dept = scanner.nextLine().trim();
        if (dept.isEmpty()) {
            dept = "General";
        }
        int year = readInt("Year of study (1–4): ", 1, 4);
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();

        Student s = new Student(id, name, password, dept, year, email, phone);
        StudentService.getInstance().addStudent(s);
        System.out.println("Registration successful. You can log in with your student ID.");
    }

    private void adminMenu() {
        int c;
        do {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. List all students");
            System.out.println("2. Delete student by ID");
            System.out.println("3. List / add / remove courses");
            System.out.println("4. List / add books");
            System.out.println("5. List / add hostel rooms");
            System.out.println("6. Help desk: view queue & resolve next");
            System.out.println("7. Events: list / add sample");
            System.out.println("8. Logout");
            c = readInt("Choose: ", 1, 8);
            switch (c) {
                case 1:
                    listStudentsAdmin();
                    break;
                case 2:
                    deleteStudentAdmin();
                    break;
                case 3:
                    adminCourses();
                    break;
                case 8:
                    System.out.println("Logging out...");
                    break;
                default:
                    break;
            }
        } while (c != 8);
    }

    private void listStudentsAdmin() {
        CustomArrayList<Student> list = StudentService.getInstance().getAllStudents();
        if (list.isEmpty()) {
            System.out.println("No students.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }

    private void deleteStudentAdmin() {
        System.out.print("Student ID to delete: ");
        String id = scanner.nextLine().trim();
        if (StudentService.getInstance().deleteStudent(id)) {
            System.out.println("Deleted.");
        } else {
            System.out.println("Student not found.");
        }
    }

    private void adminCourses() {
        System.out.println("1) List  2) Add  3) Remove");
        int a = readInt("Sub-option: ", 1, 3);
        CourseService cs = CourseService.getInstance();
        if (a == 1) {
            CustomArrayList<Course> all = cs.getAllCourses();
            if (all.isEmpty()) {
                System.out.println("No courses.");
            } else {
                for (int i = 0; i < all.size(); i++) {
                    System.out.println(all.get(i));
                }
            }
        } else if (a == 2) {
            System.out.print("Course code: ");
            String code = scanner.nextLine().trim();
            System.out.print("Course name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Instructor: ");
            String inst = scanner.nextLine().trim();
            int credits = readInt("Credits: ", 1, 12);
            int cap = readInt("Max capacity: ", 1, 500);
            System.out.print("Schedule (e.g. Mon 10:00): ");
            String sched = scanner.nextLine().trim();
            if (sched.isEmpty()) {
                sched = "TBA";
            }
            cs.addCourse(new Course(code, name, inst, credits, cap, sched));
            System.out.println("Course added.");
        } else {
            System.out.print("Course code to remove: ");
            String code = scanner.nextLine().trim();
            if (cs.deleteCourse(code)) {
                System.out.println("Removed.");
            } else {
                System.out.println("Not found.");
            }
        }
    }
    private void studentMenu(Student student) {
        int c;
        do {
            System.out.println("\n===== STUDENT MENU (" + student.getName() + ") =====");
            System.out.println("1. Courses (list & register)");
            System.out.println("2. Library (list & borrow / return)");
            System.out.println("3. Hostel (rooms & allocate)");
            System.out.println("4. Help desk (submit ticket)");
            System.out.println("5. Events (list & register)");
            System.out.println("6. Logout");
            c = readInt("Choose: ", 1, 6);
            switch (c) {
                case 1:
                    studentCourses(student);
                    break;
                case 6:
                    System.out.println("Logging out...");
                    break;
                default:
                    break;
            }
        } while (c != 6);
    }

    private void studentCourses(Student student) {
        CourseService cs = CourseService.getInstance();
        CustomArrayList<Course> courses = cs.getAllCourses();
        if (courses.isEmpty()) {
            System.out.println("No courses available.");
            return;
        }
        for (int i = 0; i < courses.size(); i++) {
            System.out.println(courses.get(i));
        }
        System.out.println("1) Register  2) Drop");
        int a = readInt("Choice: ", 1, 2);
        System.out.print("Course code: ");
        String code = scanner.nextLine().trim();
        if (a == 1) {
            if (cs.enrollStudent(student.getId(), code)) {
                System.out.println("Registered.");
            } else {
                System.out.println("Could not register (full, duplicate, or invalid code).");
            }
        } else {
            if (cs.dropStudent(student.getId(), code)) {
                System.out.println("Dropped.");
            } else {
                System.out.println("Could not drop.");
            }
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v >= min && v <= max) {
                    return v;
                }
            } catch (NumberFormatException ignored) {
                // retry
            }
            System.out.println("Enter a number between " + min + " and " + max + ".");
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return LocalDateTime.parse(line);
            } catch (Exception e) {
                System.out.println("Use format yyyy-MM-ddTHH:mm (e.g. 2026-05-01T09:00)");
            }
        }
    }
}
