package model;

/**
 * Abstract base class representing a user in the Smart Campus Management System.
 * This class provides common properties and behaviors for all user types.
 * 
 * In Java, an abstract class cannot be instantiated directly - it must be extended
 * by concrete subclasses like Student and Admin. This follows the principle of
 * inheritance where common code is shared among related classes.
 * 
 * Key Concepts Demonstrated:
 * - Abstract class (cannot create User objects directly)
 * - Protected access modifier (accessible to subclasses)
 * - Encapsulation (private data with public getters/setters)
 * - Constructor (initializes object state)
 * - Method overriding (toString() from Object class)
 */
public abstract class User {
    
    /** Unique identifier for the user in the system */
    protected String id;
    
    /** Full name of the user */
    protected String name;
    
    /** User's password for authentication (stored as plain text for demo purposes) */
    protected String password;
    
    /** User role in the system: "admin" or "student" */
    protected String role;

    /**
     * Constructor for creating a new user.
     * The constructor is called when creating Student or Admin objects.
     * 
     * @param id Unique user identifier
     * @param name Full name of the user
     * @param password User's password for login
     * @param role User's role ("admin" or "student")
     */
    public User(String id, String name, String password, String role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    // Getter and Setter Methods
    // These methods follow JavaBean conventions for accessing private fields
    
    /** @return the unique user ID */
    public String getId() { return id; }
    
    /** @param id the new user ID to set */
    public void setId(String id) { this.id = id; }

    /** @return the user's full name */
    public String getName() { return name; }
    
    /** @param name the new name to set */
    public void setName(String name) { this.name = name; }

    /** @return the user's password */
    public String getPassword() { return password; }
    
    /** @param password the new password to set */
    public void setPassword(String password) { this.password = password; }

    /** @return the user's role ("admin" or "student") */
    public String getRole() { return role; }
    
    /** @param role the new role to set */
    public void setRole(String role) { this.role = role; }

    /**
     * Verifies if the provided password matches the user's stored password.
     * This method is used during login authentication.
     * 
     * @param inputPassword The password entered by the user during login
     * @return true if passwords match, false otherwise
     */
    public boolean verifyPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    /**
     * Returns a string representation of the user.
     * This method overrides the default toString() method from the Object class.
     * It's useful for debugging and displaying user information.
     * 
     * @return Formatted string with user details
     */
    @Override
    public String toString() {
        return String.format("ID: %s, Name: %s, Role: %s", id, name, role);
    }
}