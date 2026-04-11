package persistance;

import java.io.*;
import java.util.ArrayList;
import model.Student;

public class StudentFileHandler {

    private static final String FILE_PATH = "data/students.txt";

    public static void saveStudent(Student student) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(student.toFileString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving student.");
        }
    }

    public static ArrayList<Student> loadStudents() {
        ArrayList<Student> students = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");

                Student student = new Student(
                        parts[0], parts[1], parts[2], parts[3]);

                students.add(student);
            }

        } catch (IOException e) {
            System.out.println("Error loading students.");
        }

        return students;
    }
}