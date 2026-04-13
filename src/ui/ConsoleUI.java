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
        System.out.println();
        printHeader("SMART CAMPUS MANAGEMENT SYSTEM");
        System.out.println("1. Login as Admin");
        System.out.println("2. Login as Student");
        System.out.println("3. Register New Student");
        System.out.println("4. Exit");
        printDivider();
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
        System.out.print("Student Number: ");
        String studentNumber = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        User u = LoginService.getInstance().login(studentNumber, pass);
        if (u instanceof Student) {
            System.out.println("Login successful.");
            studentMenu((Student) u);
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    private void registerStudent() {
        System.out.print("Student Number: ");
        String studentNumber = scanner.nextLine().trim();
        if (studentNumber.isEmpty()) {
            System.out.println("Student number cannot be empty.");
            return;
        }
        if (StudentService.getInstance().getStudent(studentNumber) != null) {
            System.out.println("Student already exists.");
            return;
        }
        System.out.print("Full name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Gender (Male/Female/Other): ");
        String gender = scanner.nextLine().trim();
        if (gender.isEmpty()) {
            gender = "Unspecified";
        }
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

        Student s = new Student(studentNumber, name, password, gender, dept, year, email, phone);
        StudentService.getInstance().addStudent(s);
        System.out.println("Registration successful. You can log in with your student number.");
    }

    private void adminMenu() {
        int c;
        do {
            printAdminDashboard();
            c = readInt("Choose option: ", 1, 7);
            switch (c) {
                case 1:
                    showNotImplemented("Manage Students");
                    break;
                case 2:
                    adminCourses();
                    break;
                case 3:
                    showNotImplemented("Manage Library");
                    break;
                case 4:
                    showNotImplemented("Manage Hostels");
                    break;
                case 5:
                    showNotImplemented("View Help Desk Tickets");
                    break;
                case 6:
                    showNotImplemented("Manage Events");
                    break;
                case 7:
                    System.out.println("Logging out...");
                    break;
                default:
                    break;
            }
        } while (c != 7);
    }

    private void adminCourses() {
        System.out.println();
        printHeader("MANAGE COURSES");
        System.out.println("1. List Courses");
        System.out.println("2. Add Course");
        System.out.println("3. Remove Course");
        System.out.println("4. Back");
        printDivider();
        int a = readInt("Choose option: ", 1, 4);
        CourseService cs = CourseService.getInstance();
        if (a == 1) {
            CustomArrayList<Course> all = cs.getAllCourses();
            if (all.isEmpty()) {
                System.out.println("No courses.");
            } else {
                for (int i = 0; i < all.size(); i++) {
                    System.out.println((i + 1) + ". " + all.get(i));
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
        } else if (a == 3) {
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
            printStudentPortal();
            c = readInt("Choose option: ", 1, 7);
            switch (c) {
                case 1:
                    viewStudentProfile(student);
                    break;
                case 2:
                    studentCourses(student);
                    break;
                case 3:
                    showNotImplemented("Library Services");
                    break;
                case 4:
                    showNotImplemented("Hostel Application");
                    break;
                case 5:
                    showNotImplemented("Help Desk Ticket");
                    break;
                case 6:
                    showNotImplemented("Event Booking");
                    break;
                case 7:
                    System.out.println("Logging out...");
                    break;
                default:
                    break;
            }
        } while (c != 7);
    }

    private void studentCourses(Student student) {
        CourseService cs = CourseService.getInstance();
        CustomArrayList<Course> courses = cs.getAvailableCourses();
        CustomArrayList<String> enrolledCodes = student.getEnrolledCourses();

        System.out.println();
        printHeader("COURSE REGISTRATION");

        System.out.println("Available Courses:");
        if (courses.isEmpty()) {
            System.out.println("No courses available.");
        } else {
            for (int i = 0; i < courses.size(); i++) {
                Course c = courses.get(i);
                System.out.println((i + 1) + ". " + c + " | Credits: " + c.getCredits() + " | " + c.getSchedule());
            }
        }

        System.out.println("\nMy Registered Courses:");
        if (enrolledCodes.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < enrolledCodes.size(); i++) {
                String code = enrolledCodes.get(i);
                Course course = cs.getCourse(code);
                if (course != null) {
                    System.out.println((i + 1) + ". " + course);
                } else {
                    System.out.println((i + 1) + ". " + code);
                }
            }
        }

        System.out.println("\n1. Register for a Course");
        System.out.println("2. Drop a Course");
        System.out.println("3. Back");
        printDivider();
        int a = readInt("Choose option: ", 1, 3);
        if (a == 3) {
            return;
        }

        System.out.print("Course code: ");
        String code = scanner.nextLine().trim();
        if (a == 1) {
            if (cs.enrollStudent(student.getStudentNumber(), code)) {
                System.out.println("Registered.");
            } else {
                System.out.println("Could not register (full, duplicate, or invalid code).");
            }
        } else {
            if (cs.dropStudent(student.getStudentNumber(), code)) {
                System.out.println("Dropped.");
            } else {
                System.out.println("Could not drop.");
            }
        }
    }

    private void viewStudentProfile(Student student) {
        System.out.println();
        printHeader("MY PROFILE");
        System.out.println("Student No.: " + student.getStudentNumber());
        System.out.println("Name       : " + student.getName());
        System.out.println("Gender     : " + student.getGender());
        System.out.println("Department : " + student.getDepartment());
        System.out.println("Year       : " + student.getYear());
        System.out.println("Email      : " + student.getEmail());
        System.out.println("Phone      : " + student.getPhone());
        System.out.println("Courses    : " + student.getEnrolledCourses().size());
        printDivider();
    }

    private void printAdminDashboard() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║           ADMIN DASHBOARD             ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║  1. Manage Students                   ║");
        System.out.println("║  2. Manage Courses                    ║");
        System.out.println("║  3. Manage Library                    ║");
        System.out.println("║  4. Manage Hostels                    ║");
        System.out.println("║  5. View Help Desk Tickets            ║");
        System.out.println("║  6. Manage Events                     ║");
        System.out.println("║  7. Logout                            ║");
        System.out.println("╚════════════════════════════════════════╝");
    }

    private void printStudentPortal() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║           STUDENT PORTAL              ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║  1. View My Profile                   ║");
        System.out.println("║  2. Course Registration               ║");
        System.out.println("║  3. Library Services                  ║");
        System.out.println("║  4. Hostel Application                ║");
        System.out.println("║  5. Submit Help Desk Ticket           ║");
        System.out.println("║  6. Event Booking                     ║");
        System.out.println("║  7. Logout                            ║");
        System.out.println("╚════════════════════════════════════════╝");
    }

    private void printHeader(String title) {
        System.out.println("========================================");
        System.out.println("  " + title);
        System.out.println("========================================");
    }

    private void printDivider() {
        System.out.println("----------------------------------------");
    }

    private void showNotImplemented(String moduleName) {
        System.out.println(moduleName + " is not implemented in this phase.");
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
