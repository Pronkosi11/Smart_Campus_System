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
 * ConsoleUI - Main User Interface Controller
 * 
 * This class serves as the primary UI router for the Smart Campus Management System.
 * It provides the main menu system and coordinates between different service modules.
 * 
 * Key responsibilities:
 * - Display main menu and handle user authentication
 * - Route users to appropriate dashboards (Admin/Student)
 * - Coordinate with service classes for business logic
 * - Provide consistent BoxUI-based interface throughout the application
 * - Handle user registration and login flows
 * 
 * The class uses the BoxUI component to ensure a clean, professional console interface
 * with proper formatting, input validation, and error handling.
 */
public class ConsoleUI {

    // Core UI components for user interaction
    private final Scanner scanner = new Scanner(System.in);
    private final ui.BoxUI box = new BoxUI(scanner);

    // Service layer instances - using singleton pattern for centralized state management
    private final StudentService studentService = StudentService.getInstance();      // Handles student CRUD operations and profile management
    private final CourseService courseService = CourseService.getInstance();        // Manages course catalog and enrollment
    private final LibraryService libraryService = LibraryService.getInstance();     // Library services (placeholder for future implementation)
    private final HostelService hostelService = HostelService.getInstance();        // Hostel management (placeholder for future implementation)
    private final HelpDeskService helpDeskService = HelpDeskService.getInstance();  // Support ticket system (fully implemented)
    private final EventService eventService = EventService.getInstance();          // Event management (placeholder for future implementation)

    /**
     * Main application loop that displays the welcome menu and handles user choices.
     * 
     * This method provides the primary entry point for user interaction, offering:
     * - Admin login for administrative functions
     * - Student login for student portal access
     * - New student registration
     * - Clean exit option
     * 
     * The loop continues until the user chooses to exit (option 4).
     */
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

    /**
     * Displays the main welcome menu with system title and available options.
     * 
     * This method renders the primary navigation menu using BoxUI formatting,
     * providing users with clear options for system access and registration.
     */
    private void printMainMenu() {
        box.printMenu("SMART CAMPUS MANAGEMENT SYSTEM", new String[]{
                "1. Login as Admin",
                "2. Login as Student",
                "3. Register New Student",
                "4. Exit"
        });
    }

    /**
     * Handles administrator login authentication and routing.
     * 
     * This method:
     * - Prompts for admin username and password
     * - Validates credentials through LoginService
     * - Routes successful admin logins to the admin dashboard
     * - Displays appropriate error messages for failed attempts
     */
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

    /**
     * Handles student login authentication and routing.
     * 
     * This method:
     * - Prompts for student number and password
     * - Validates credentials through LoginService
     * - Routes successful student logins to the student portal
     * - Displays appropriate error messages for failed attempts
     */
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

    /**
     * Handles new student registration process with comprehensive data collection.
     * 
     * This method:
     * - Validates student number uniqueness
     * - Collects all required student information (name, password, demographics, contact info)
     * - Provides sensible defaults for optional fields (gender, department)
     * - Creates and registers the new student through StudentService
     * - Provides feedback on registration success/failure
     */
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

    /**
     * Administrator dashboard menu loop that provides access to all admin functions.
     * 
     * This method displays the admin dashboard and routes to various administrative modules:
     * - Student management (view, add, delete students)
     * - Course management (create, modify courses)
     * - Library management (placeholder for future implementation)
     * - Hostel management (placeholder for future implementation)
     * - Help desk ticket management (fully functional)
     * - Event management (placeholder for future implementation)
     * 
     * The loop continues until the admin chooses to logout (option 7).
     */
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
                    libraryService.showAdminLibraryMenu(box);
                    break;
                case 4:
                    hostelService.showAdminHostelMenu(box);
                    break;
                case 5:
                    helpDeskService.showAdminHelpDeskMenu(box);
                    break;
                case 6:
                    eventService.showAdminEventMenu(box);
                    break;
                case 7:
                    box.info("Logging out...");
                    break;
                default:
                    break;
            }
        } while (c != 7);
    }

    /**
     * Student portal menu loop that provides access to all student functions.
     * 
     * This method displays the student portal and routes to various student services:
     * - Profile viewing (personal information display)
     * - Course registration (enroll/drop courses)
     * - Library services (placeholder for future implementation)
     * - Hostel application (placeholder for future implementation)
     * - Help desk ticket submission and tracking (fully functional)
     * - Event booking (placeholder for future implementation)
     * 
     * The loop continues until the student chooses to logout (option 7).
     * 
     * @param student The authenticated student object for personalized access
     */
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
                    libraryService.showStudentLibraryMenu(box, student);
                    break;
                case 4:
                    hostelService.showStudentHostelMenu(box, student);
                    break;
                case 5:
                    helpDeskService.showStudentHelpDeskMenu(box, student);
                    break;
                case 6:
                    eventService.showStudentEventMenu(box, student);
                    break;
                case 7:
                    box.info("Logging out...");
                    break;
                default:
                    break;
            }
        } while (c != 7);
    }

    /**
     * Displays the administrator dashboard menu with all available admin options.
     * 
     * This method renders a clean, formatted menu showing all administrative
     * functions available in the system, including both implemented and
     * planned features.
     */
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

    /**
     * Displays the student portal menu with all available student options.
     * 
     * This method renders a clean, formatted menu showing all student
     * services available in the system, including both implemented and
     * planned features.
     */
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
