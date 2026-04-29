package service;

import datastructures.CustomArrayList;
import datastructures.CustomHashMap;
import model.Course;
import model.Student;
import persistence.DataPersistence;
import ui.BoxUI;

/**
 * CourseService - Course Management Service
 * 
 * This service class handles all course-related operations in the Smart Campus System.
 * It provides comprehensive course management functionality including:
 * 
 * Core Operations:
 * - Course creation, retrieval, update, and deletion (CRUD operations)
 * - Course enrollment and student management
 * - Course catalog management and searching
 * - Integration with student enrollment system
 * 
 * UI Integration:
 * - Admin interface for course management
 * - Student course registration interface
 * - Clean BoxUI-based console interface
 * 
 * Data Management:
 * - Uses CustomHashMap for efficient course storage (O(1) lookup by course code)
 * - Automatic persistence through DataPersistence layer
 * - Integration with StudentService for enrollment tracking
 * 
 * Key Concepts Demonstrated:
 * - Singleton design pattern (single instance across application)
 * - Service layer architecture (separates business logic from UI)
 * - Data persistence integration
 * - Custom data structure usage
 * - Error handling and validation
 * 
 * This class follows the Singleton pattern to ensure centralized course data management
 * across the entire application, preventing data inconsistencies.
 */
public class CourseService {
    
    /** 
     * Primary storage for all courses - indexed by course code for fast lookup.
     * Using CustomHashMap provides O(1) average time complexity for course retrieval.
     */
    private CustomHashMap<String, Course> courses;
    
    /** 
     * Singleton instance to ensure centralized course management.
     * This ensures all parts of the application work with the same course data.
     */
    private static CourseService instance;

    /**
     * Private constructor to enforce singleton pattern.
     * 
     * This constructor is private to prevent direct instantiation.
     * Use getInstance() to get the singleton instance.
     * Initializes the course storage HashMap.
     */
    private CourseService() {
        courses = new CustomHashMap<>();
    }

    /**
     * Gets the singleton instance of CourseService.
     * 
     * This method implements the lazy initialization singleton pattern:
     * - If no instance exists, create one
     * - Return the existing instance
     * 
     * This ensures only one CourseService exists throughout the application.
     * 
     * @return The singleton CourseService instance
     */
    public static CourseService getInstance() {
        if (instance == null) {
            instance = new CourseService();
        }
        return instance;
    }

    // ========== CRUD OPERATIONS ==========
    
    /**
     * Adds a new course to the system.
     * 
     * This method performs course creation with automatic persistence:
     * 1. Stores the course in the HashMap using course code as key
     * 2. Saves the updated course data to persistent storage
     * 
     * @param course The Course object to add to the system
     */
    public void addCourse(Course course) {
        courses.put(course.getCourseCode(), course);
        DataPersistence.saveCourses(courses);
    }

    /**
     * Retrieves a course by its unique course code.
     * 
     * This method provides fast O(1) lookup using the HashMap.
     * 
     * @param courseCode The unique course code to search for
     * @return The Course object if found, null if not found
     */
    public Course getCourse(String courseCode) {
        return courses.get(courseCode);
    }

    /**
     * Updates an existing course in the system.
     * 
     * This method updates course information and persists the changes:
     * 1. Replaces the existing course in the HashMap
     * 2. Saves the updated course data to persistent storage
     * 
     * @param course The Course object with updated information
     */
    public void updateCourse(Course course) {
        courses.put(course.getCourseCode(), course);
        DataPersistence.saveCourses(courses);
    }

    /**
     * Deletes a course from the system.
     * 
     * This method removes a course and persists the change:
     * 1. Removes the course from the HashMap
     * 2. If successful, saves the updated data
     * 3. Returns whether the deletion was successful
     * 
     * @param courseCode The course code of the course to delete
     * @return true if course was deleted, false if course was not found
     */
    public boolean deleteCourse(String courseCode) {
        Course removed = courses.remove(courseCode);
        if (removed != null) {
            DataPersistence.saveCourses(courses);
            return true;
        }
        return false;
    }

    /**
     * Retrieves all courses in the system.
     * 
     * This method returns a collection of all courses for display purposes.
     * The returned CustomArrayList can be used for listing, searching, or filtering.
     * 
     * @return CustomArrayList containing all Course objects in the system
     */
    public CustomArrayList<Course> getAllCourses() {
        return courses.values();
    }

    // ========== ENROLLMENT MANAGEMENT METHODS ==========
    
    /**
     * Retrieves all courses that have available enrollment slots.
     * 
     * This method filters out courses that are at maximum capacity,
     * returning only courses that new students can enroll in.
     * 
     * @return CustomArrayList of courses that are not full
     */
    public CustomArrayList<Course> getAvailableCourses() {
        CustomArrayList<Course> available = new CustomArrayList<>();
        CustomArrayList<Course> allCourses = courses.values();

        // Iterate through all courses and check availability
        for (Course course : allCourses) {
            if (!course.isFull()) {
                available.add(course);
            }
        }
        return available;
    }

    /**
     * Enrolls a student in a course.
     * 
     * This method performs a complete enrollment transaction:
     * 1. Validates that both student and course exist
     * 2. Checks if course has available capacity
     * 3. Enrolls student in the course
     * 4. Updates student's enrollment record
     * 5. Persists all changes to storage
     * 
     * This method ensures data consistency by updating both the course
     * and student records atomically.
     * 
     * @param studentNumber The student's unique identifier
     * @param courseCode The course's unique identifier
     * @return true if enrollment was successful, false otherwise
     */
    public boolean enrollStudent(String studentNumber, String courseCode) {
        // Retrieve course and student objects
        Course course = courses.get(courseCode);
        Student student = StudentService.getInstance().getStudent(studentNumber);

        // Validate that both exist and course has capacity
        if (course != null && student != null && !course.isFull()) {
            // Attempt to enroll student in course
            if (course.enrollStudent(studentNumber)) {
                // Update student's enrollment record
                student.enrollCourse(courseCode);
                StudentService.getInstance().updateStudent(student);
                
                // Persist changes
                DataPersistence.saveCourses(courses);
                return true;
            }
        }
        return false;
    }

    /**
     * Drops a student from a course.
     * 
     * This method performs a complete drop transaction:
     * 1. Validates that both student and course exist
     * 2. Removes student from course enrollment
     * 3. Updates student's enrollment record
     * 4. Persists all changes to storage
     * 
     * This method ensures data consistency by updating both the course
     * and student records atomically.
     * 
     * @param studentNumber The student's unique identifier
     * @param courseCode The course's unique identifier
     * @return true if drop was successful, false otherwise
     */
    public boolean dropStudent(String studentNumber, String courseCode) {
        // Retrieve course and student objects
        Course course = courses.get(courseCode);
        Student student = StudentService.getInstance().getStudent(studentNumber);

        // Validate that both exist
        if (course != null && student != null) {
            // Attempt to drop student from course
            if (course.dropStudent(studentNumber)) {
                // Update student's enrollment record
                student.dropCourse(courseCode);
                StudentService.getInstance().updateStudent(student);
                
                // Persist changes
                DataPersistence.saveCourses(courses);
                return true;
            }
        }
        return false;
    }

    // ========== DATA MANAGEMENT METHODS ==========
    
    /**
     * Sets the courses data from loaded persistence data.
     * 
     * This method is used by the persistence layer to restore course data
     * when the application starts up.
     * 
     * @param loadedCourses The courses data loaded from storage
     */
    public void setCourses(CustomHashMap<String, Course> loadedCourses) {
        this.courses = loadedCourses;
    }

    /**
     * Gets the internal courses HashMap.
     * 
     * This method provides access to the underlying data structure for
     * persistence operations and other system components.
     * 
     * @return The internal HashMap containing all courses
     */
    public CustomHashMap<String, Course> getCoursesMap() {
        return courses;
    }

    // ========== USER INTERFACE METHODS ==========
    
    /**
     * Displays the admin course management menu.
     * 
     * This method provides the main interface for administrators to manage courses:
     * - List all courses in the system
     * - Add new courses to the catalog
     * - Remove existing courses
     * - Return to admin dashboard
     * 
     * The menu uses a do-while loop to persist until the user chooses to exit,
     * providing a continuous workflow for course management tasks.
     * 
     * @param box The BoxUI instance for rendering the interface
     */
    public void showAdminCoursesMenu(BoxUI box) {
        int choice;
        do {
            // Display the course management menu options
            box.printMenu("MANAGE COURSES", new String[]{
                    "1. List Courses",
                    "2. Add Course", 
                    "3. Remove Course",
                    "4. Back"
            });
            
            // Get user's menu choice with validation
            choice = box.readInt("Choose option: ", 1, 4);
            
            // Execute the selected action
            switch (choice) {
                case 1:
                    listCourses(box);      // Display all courses
                    break;
                case 2:
                    addCourse(box);        // Add a new course
                    break;
                case 3:
                    removeCourse(box);     // Remove an existing course
                    break;
                case 4:
                    // Back to admin dashboard - just exit the loop
                    break;
                default:
                    // This case shouldn't occur due to readInt validation
                    break;
            }
        } while (choice != 4);  // Continue until user chooses "Back"
    }

    /**
     * Displays the student course registration interface.
     * 
     * This method provides a comprehensive course registration interface for students:
     * - Shows all available courses with details
     * - Displays student's current enrollments
     * - Allows course registration and dropping
     * - Provides real-time feedback on operations
     * 
     * The interface uses a do-while loop to allow multiple registration/drop actions
     * before returning to the student dashboard.
     * 
     * @param box The BoxUI instance for rendering the interface
     * @param student The student who is registering for courses
     */
    public void showStudentCourseRegistration(BoxUI box, Student student) {
        int choice;
        do {
            // Get available courses and student's current enrollments
            CustomArrayList<Course> courses = getAvailableCourses();
            CustomArrayList<String> enrolledCodes = student.getEnrolledCourses();

            // Build preview lines for optimal box sizing
            String[] previewLines = buildCourseRegistrationLines(courses, enrolledCodes);
            box.printSection("COURSE REGISTRATION", previewLines);

            // Display available courses
            box.line("Available Courses:");
            if (courses.isEmpty()) {
                box.line("- No courses available.");
            } else {
                for (int i = 0; i < courses.size(); i++) {
                    Course c = courses.get(i);
                    box.line((i + 1) + ". " + c + " | Credits: " + c.getCredits() + " | " + c.getSchedule());
                }
            }

            // Display student's current enrollments
            box.line("");
            box.line("My Registered Courses:");
            if (enrolledCodes.isEmpty()) {
                box.line("- None");
            } else {
                for (int i = 0; i < enrolledCodes.size(); i++) {
                    String code = enrolledCodes.get(i);
                    Course course = getCourse(code);
                    if (course != null) {
                        box.line((i + 1) + ". " + course);
                    } else {
                        box.line((i + 1) + ". " + code);
                    }
                }
            }
            box.endSection();

            // Display action menu
            box.printMenu("COURSE ACTIONS", new String[]{
                    "1. Register for a Course",
                    "2. Drop a Course",
                    "3. Back"
            });
            choice = box.readInt("Choose option: ", 1, 3);
            
            // Exit if user chose "Back"
            if (choice == 3) {
                break;
            }

            // Get course code from user
            String code = box.prompt("Course code: ");
            
            // Process the selected action
            if (choice == 1) {
                // Attempt to register for course
                if (enrollStudent(student.getStudentNumber(), code)) {
                    box.success("Registered.");
                } else {
                    box.error("Could not register (full, duplicate, or invalid code).");
                }
            } else if (choice == 2) {
                // Attempt to drop course
                if (dropStudent(student.getStudentNumber(), code)) {
                    box.success("Dropped.");
                } else {
                    box.error("Could not drop.");
                }
            }
        } while (choice != 3);  // Continue until user chooses "Back"
    }

    // ========== PRIVATE HELPER METHODS ==========
    
    /**
     * Displays a list of all courses in the system.
     * 
     * This helper method shows all courses in a formatted box display.
     * It's used by the admin menu to provide an overview of the course catalog.
     * 
     * @param box The BoxUI instance for rendering the interface
     */
    private void listCourses(BoxUI box) {
        // Get all courses from the system
        CustomArrayList<Course> all = getAllCourses();
        
        // Handle empty course list
        if (all.isEmpty()) {
            box.info("No courses.");
            return;
        }
        
        // Prepare formatted lines for display
        String[] lines = new String[all.size()];
        for (int i = 0; i < all.size(); i++) {
            lines[i] = (i + 1) + ". " + all.get(i);
        }
        
        // Display the course list in a formatted box
        box.printSection("COURSE LIST", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    /**
     * Handles the process of adding a new course to the system.
     * 
     * This helper method guides the admin through course creation:
     * 1. Collects course information through prompts
     * 2. Validates input using BoxUI's readInt method
     * 3. Creates the Course object
     * 4. Adds it to the system
     * 5. Provides feedback on success
     * 
     * @param box The BoxUI instance for user interaction
     */
    private void addCourse(BoxUI box) {
        // Collect course information from admin
        String code = box.prompt("Course code: ");
        String name = box.prompt("Course name: ");
        String instructor = box.prompt("Instructor: ");
        int credits = box.readInt("Credits: ", 1, 12);           // Validate 1-12 credits
        int maxCapacity = box.readInt("Max capacity: ", 1, 500); // Validate 1-500 capacity
        String schedule = box.prompt("Schedule (e.g. Mon 10:00): ");
        
        // Handle empty schedule with default value
        if (schedule.isEmpty()) {
            schedule = "TBA";
        }
        
        // Create and add the course
        addCourse(new Course(code, name, instructor, credits, maxCapacity, schedule));
        box.success("Course added.");
    }

    /**
     * Handles the process of removing a course from the system.
     * 
     * This helper method:
     * 1. Prompts for the course code to remove
     * 2. Attempts to delete the course
     * 3. Provides appropriate feedback
     * 
     * @param box The BoxUI instance for user interaction
     */
    private void removeCourse(BoxUI box) {
        String code = box.prompt("Course code to remove: ");
        if (deleteCourse(code)) {
            box.success("Removed.");
        } else {
            box.error("Not found.");
        }
    }

    /**
     * Builds preview lines for optimal box sizing in course registration.
     * 
     * This helper method creates an array of strings that represents the content
     * that will be displayed in the course registration interface. This allows
     * the BoxUI to calculate the optimal box width before actually displaying the content.
     * 
     * The method includes:
     * - Available courses section header and course details
     * - Enrolled courses section header and course details
     * - Proper handling of empty sections
     * 
     * @param courses List of available courses
     * @param enrolledCodes List of course codes the student is enrolled in
     * @return Array of strings representing the content to be displayed
     */
    private String[] buildCourseRegistrationLines(CustomArrayList<Course> courses,
                                                  CustomArrayList<String> enrolledCodes) {
        // Calculate total lines needed
        int count = 3 + courses.size() + Math.max(1, enrolledCodes.size());
        String[] lines = new String[count];
        int idx = 0;
        
        // Add available courses section
        lines[idx++] = "Available Courses:";
        if (courses.isEmpty()) {
            lines[idx++] = "- No courses available.";
        } else {
            for (int i = 0; i < courses.size(); i++) {
                Course c = courses.get(i);
                lines[idx++] = (i + 1) + ". " + c + " | Credits: " + c.getCredits() + " | " + c.getSchedule();
            }
        }
        
        // Add enrolled courses section
        lines[idx++] = "My Registered Courses:";
        if (enrolledCodes.isEmpty()) {
            lines[idx++] = "- None";
        } else {
            for (int i = 0; i < enrolledCodes.size(); i++) {
                String code = enrolledCodes.get(i);
                Course course = getCourse(code);
                lines[idx++] = (i + 1) + ". " + (course != null ? course.toString() : code);
            }
        }
        
        return lines;
    }
}