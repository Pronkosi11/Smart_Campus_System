package model;

/**
 * Admin class representing an administrator user in the Smart Campus Management System.
 * 
 * This class extends the User class to create administrator accounts with full system access.
 * Admin users can manage all aspects of the campus system including students, courses,
 * library books, hostel rooms, help desk tickets, and events.
 * 
 * Key Concepts Demonstrated:
 * - Inheritance (extends User class)
 * - Constructor chaining (using super() to call parent constructor)
 * - Method overriding (custom toString() implementation)
 * - Role-based access control (ADMIN role)
 * 
 * The Admin class is simpler than Student because administrators don't need
 * additional attributes like courses, books, or room assignments.
 */
public class Admin extends User {

    /**
     * Constructor for creating a new admin user.
     * 
     * This constructor calls the parent User class constructor using super() to
     * initialize the basic user information. It automatically sets the role to "ADMIN".
     * 
     * @param id Unique admin identifier (used as login username)
     * @param name Full name of the administrator
     * @param password Login password for authentication
     */
    public Admin(String id, String name, String password) {
        // Call the parent constructor with role set to "ADMIN"
        super(id, name, password, "ADMIN");
    }

    /**
     * Returns a string representation of the admin for display purposes.
     * 
     * This method overrides the default toString() method from the User class.
     * It adds an "Admin - " prefix to clearly identify this as an administrator account.
     * 
     * @return Formatted string with admin information
     */
    @Override
    public String toString() {
        return String.format("Admin - %s", super.toString());
    }
}