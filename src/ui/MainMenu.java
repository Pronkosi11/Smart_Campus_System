package ui;

import java.util.Scanner;
import model.Student;
import persistance.StudentFileHandler;
import service.StudentService;
import model.Student;

public class MainMenu {

    private Scanner scanner = new Scanner(System.in);
    private StudentService studentService = new StudentService();

    public void start() {
        int choice;

        do {
            System.out.println("\n===== SMART CAMPUS SYSTEM =====");
            System.out.println("1. Register Student");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");

            choice = scanner.nextInt();
            scanner.nextLine(); // clear buffer

            switch (choice) {
                case 1:
                    System.out.print("Student Number: ");
                    String number = scanner.nextLine();

                    System.out.print("Name: ");
                    String name = scanner.nextLine();

                    System.out.print("Surname: ");
                    String surname = scanner.nextLine();

                    System.out.print("Password: ");
                    String password = scanner.nextLine();

                    Student student = new Student(number, name, surname, password);
                    StudentFileHandler.saveStudent(student);

                    System.out.println("Student registered successfully!");
                    break;
                case 2:
                    System.out.print("Student Number: ");
                    String loginNumber = scanner.nextLine();

                    System.out.print("Password: ");
                    String loginPassword = scanner.nextLine();

                    Student loggedIn = studentService.login(loginNumber, loginPassword);

                    if (loggedIn != null) {
                        System.out.println("Login successful! Welcome " + loginNumber);

                        // 👉 go to student menu (next step)
                        //studentMenu(loggedIn);

                    } else {
                        System.out.println("Invalid student number or password!");
                    }
                    break;
                case 3:
                    System.out.println("Hostel module coming soon...");
                    break;
                case 4:
                    System.out.println("Help Desk module coming soon...");
                    break;
                case 5:
                    System.out.println("Events module coming soon...");
                    break;
                case 6:
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid option!");
            }

        } while (choice != 6);
    }
}