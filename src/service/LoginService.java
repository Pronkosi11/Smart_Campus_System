package service;

import model.*;
import datastructures.CustomHashMap;

public class LoginService {
    private CustomHashMap<String, User> users;
    private static LoginService instance;

    private LoginService() {
        users = new CustomHashMap<>();
        initializeDefaultAdmin();
    }

    public static LoginService getInstance() {
        if (instance == null) {
            instance = new LoginService();
        }
        return instance;
    }

    private void initializeDefaultAdmin() {
        // Create default admin account
        Admin admin = new Admin("admin", "System Administrator", "admin123");
        users.put(admin.getId(), admin);
    }

    public User login(String userId, String password) {
        User user = users.get(userId);

        if (user == null) {
            // Try to find student from student service
            Student student = StudentService.getInstance().getStudent(userId);
            if (student != null) {
                user = student;
                users.put(userId, student);
            }
        }

        if (user != null && user.verifyPassword(password)) {
            return user;
        }

        return null;
    }

    public void registerStudent(Student student) {
        users.put(student.getStudentNumber(), student);
    }

    public void unregisterStudent(String studentNumber) {
        users.remove(studentNumber);
    }

    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        User user = users.get(userId);
        if (user != null && user.verifyPassword(oldPassword)) {
            user.setPassword(newPassword);
            if (user instanceof Student) {
                StudentService.getInstance().updateStudent((Student) user);
            }
            return true;
        }
        return false;
    }
}