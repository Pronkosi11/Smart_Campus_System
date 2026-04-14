package ui;

import model.Admin;
import model.Student;
import model.User;
import service.EventService;
import service.HelpDeskService;
import service.HostelService;
import service.LibraryService;
import service.LoginService;
import service.CourseService;
import service.StudentService;

import java.util.Scanner;

/**
 * Top-level menu router. Module-specific UI flows live in dedicated UI classes.
 */
public class ConsoleUI {

    private final Scanner scanner = new Scanner(System.in);
    private final BoxUI box = new BoxUI(scanner);

    private final StudentService studentService = StudentService.getInstance();
    private final CourseService courseService = CourseService.getInstance();
    private final LibraryService libraryService = LibraryService.getInstance();
    private final HostelService hostelService = HostelService.getInstance();
    private final HelpDeskService helpDeskService = HelpDeskService.getInstance();
    private final EventService eventService = EventService.getInstance();

    public void start() {
        int choice;
        do {
            printMainMenu();
            choice = box.readInt("Choose option: ", 1, 4);
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
                    box.error("Invalid choice.");
            }
        } while (choice != 4);
    }

    private void printMainMenu() {
        box.printMenu("SMART CAMPUS MANAGEMENT SYSTEM", new String[]{
                "1. Login as Admin",
                "2. Login as Student",
                "3. Register New Student",
                "4. Exit"
        });
    }

    private void handleLoginAsAdmin() {
        String user = box.prompt("Username: ");
        String pass = box.prompt("Password: ");

        User u = LoginService.getInstance().login(user, pass);
        if (u instanceof Admin) {
            box.success("Admin login successful.");
            adminMenu();
        } else {
            box.error("Invalid admin credentials.");
        }
    }

    private void handleLoginAsStudent() {
        String studentNumber = box.prompt("Student Number: ");
        String pass = box.prompt("Password: ");

        User u = LoginService.getInstance().login(studentNumber, pass);
        if (u instanceof Student) {
            box.success("Login successful.");
            studentMenu((Student) u);
        } else {
            box.error("Invalid credentials.");
        }
    }

    private void registerStudent() {
        String studentNumber = box.prompt("Student Number: ");
        if (studentNumber.isEmpty()) {
            box.error("Student number cannot be empty.");
            return;
        }
        if (studentService.studentExists(studentNumber)) {
            box.error("Student already exists.");
            return;
        }

        String name = box.prompt("Full name: ");
        String password = box.prompt("Password: ");
        String gender = box.prompt("Gender (Male/Female/Other): ");
        if (gender.isEmpty()) {
            gender = "Unspecified";
        }
        String dept = box.prompt("Department: ");
        if (dept.isEmpty()) {
            dept = "General";
        }
        int year = box.readInt("Year of study (1–4): ", 1, 4);
        String email = box.prompt("Email: ");
        String phone = box.prompt("Phone: ");

        Student s = new Student(studentNumber, name, password, gender, dept, year, email, phone);
        studentService.registerStudent(s);
        box.success("Registration successful. You can log in with your student number.");
    }

    private void adminMenu() {
        int c;
        do {
            printAdminDashboard();
            c = box.readInt("Choose option: ", 1, 7);
            switch (c) {
                case 1:
                    studentService.showAdminStudentsMenu(box);
                    break;
                case 2:
                    courseService.showAdminCoursesMenu(box);
                    break;
                case 3:
                    box.info(libraryService.getAdminStatusMessage());
                    break;
                case 4:
                    box.info(hostelService.getAdminStatusMessage());
                    break;
                case 5:
                    box.info(helpDeskService.getAdminStatusMessage());
                    break;
                case 6:
                    box.info(eventService.getAdminStatusMessage());
                    break;
                case 7:
                    box.info("Logging out...");
                    break;
                default:
                    break;
            }
        } while (c != 7);
    }

    private void studentMenu(Student student) {
        int c;
        do {
            printStudentPortal();
            c = box.readInt("Choose option: ", 1, 7);
            switch (c) {
                case 1:
                    studentService.showStudentProfile(box, student);
                    break;
                case 2:
                    courseService.showStudentCourseRegistration(box, student);
                    break;
                case 3:
                    box.info(libraryService.getStudentStatusMessage());
                    break;
                case 4:
                    box.info(hostelService.getStudentStatusMessage());
                    break;
                case 5:
                    box.info(helpDeskService.getStudentStatusMessage());
                    break;
                case 6:
                    box.info(eventService.getStudentStatusMessage());
                    break;
                case 7:
                    box.info("Logging out...");
                    break;
                default:
                    break;
            }
        } while (c != 7);
    }

    private void printAdminDashboard() {
        box.printMenu("ADMIN DASHBOARD", new String[]{
                "1. Manage Students",
                "2. Manage Courses",
                "3. Manage Library",
                "4. Manage Hostels",
                "5. View Help Desk Tickets",
                "6. Manage Events",
                "7. Logout"
        });
    }

    private void printStudentPortal() {
        box.printMenu("STUDENT PORTAL", new String[]{
                "1. View My Profile",
                "2. Course Registration",
                "3. Library Services",
                "4. Hostel Application",
                "5. Submit Help Desk Ticket",
                "6. Event Booking",
                "7. Logout"
        });
    }
}
