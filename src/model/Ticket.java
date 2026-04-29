package model;

import datastructures.CustomStack;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Ticket class representing a help desk support ticket in the Smart Campus Management System.
 * 
 * This class models a support ticket submitted by students for various campus issues.
 * It tracks the ticket lifecycle from creation through resolution, maintaining a complete
 * history of status changes.
 * 
 * Key Features:
 * - Ticket identification and student association
 * - Issue tracking (subject and description)
 * - Status management workflow (open -> in-progress -> closed)
 * - Timestamp tracking for creation and resolution
 * - Complete status change history using a stack data structure
 * 
 * Key Concepts Demonstrated:
 * - Final fields (immutable data that cannot be changed after creation)
 * - Static constants (shared values accessible without creating an object)
 * - Stack data structure for tracking history (LIFO - Last In, First Out)
 * - Date/time handling with LocalDateTime
 * - Status validation and normalization
 * - Method overloading (multiple constructors)
 */
public class Ticket {
    
    // ========== STATUS CONSTANTS ==========
    // These are static final constants that define the allowed workflow states.
    // They ensure consistency across the application and prevent typos.
    
    /** Initial status when ticket is first created */
    public static final String STATUS_OPEN = "open";
    
    /** Status when admin is actively working on the ticket */
    public static final String STATUS_IN_PROGRESS = "in-progress";
    
    /** Final status when the issue has been resolved */
    public static final String STATUS_CLOSED = "closed";

    // ========== IMMUTABLE FIELDS ==========
    // These fields are marked 'final' because they should never change after creation.
    // This ensures data integrity and prevents accidental modification of core ticket information.
    
    /** Unique identifier for the ticket (e.g., "TKT-1001") */
    private final String id;
    
    /** Student number of the student who submitted this ticket */
    private final String studentNumber;
    
    /** Brief summary of the issue (like an email subject line) */
    private final String subject;
    
    /** Detailed description of the issue or problem */
    private final String description;

    // ========== MUTABLE FIELDS ==========
    // These fields can change during the ticket lifecycle.
    
    /** Current workflow status of the ticket */
    private String status;
    
    /** Timestamp when the ticket was created (never changes after creation) */
    private final LocalDateTime createdAt;
    
    /** Timestamp when the ticket was resolved (null until resolved) */
    private LocalDateTime resolvedAt;

    // ========== HISTORY TRACKING ==========
    // Stack keeps newest status change on top (LIFO - Last In, First Out).
    // This allows us to easily see the most recent changes and maintain a complete history.
    
    /** Stack containing the complete history of status changes */
    private final CustomStack<String> statusHistory;

    // ========== CONSTRUCTORS ==========
    
    /**
     * Convenience constructor for creating a new ticket from user interface input.
     * 
     * This is the primary constructor used when students submit new tickets through the UI.
     * It automatically sets the initial status to "open" and records the current timestamp.
     * 
     * Method Overloading: This class has multiple constructors with different parameters.
     * Java automatically chooses the right constructor based on the arguments provided.
     * 
     * @param id Unique ticket identifier (e.g., "TKT-1001")
     * @param studentNumber Student number of the student submitting the ticket
     * @param subject Brief summary of the issue
     * @param description Detailed description of the problem
     */
    public Ticket(String id, String studentNumber, String subject, String description) {
        // Call the full constructor with default values for new tickets
        this(id, studentNumber, subject, description, STATUS_OPEN, LocalDateTime.now(), null, new CustomStack<>());
    }

    /**
     * Full constructor used by persistence layer and for advanced initialization.
     * 
     * This constructor is primarily used when loading ticket data from JSON files.
     * It handles data restoration and ensures the ticket state is properly reconstructed.
     * 
     * @param id Unique ticket identifier
     * @param studentNumber Student number who owns this ticket
     * @param subject Brief issue summary
     * @param description Detailed problem description
     * @param status Current workflow status (will be normalized to valid value)
     * @param createdAt When the ticket was created (null defaults to now)
     * @param resolvedAt When the ticket was resolved (null if not resolved)
     * @param statusHistory Stack of previous status changes (null creates empty stack)
     */
    public Ticket(String id, String studentNumber, String subject, String description, String status,
                  LocalDateTime createdAt, LocalDateTime resolvedAt, CustomStack<String> statusHistory) {
        // Initialize immutable fields
        this.id = id;
        this.studentNumber = studentNumber;
        this.subject = subject;
        this.description = description;
        
        // Normalize and set status (ensures valid status value)
        this.status = normalizeStatus(status);
        
        // Handle creation timestamp (default to current time if null)
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        
        // Set resolution timestamp (can be null if not resolved)
        this.resolvedAt = resolvedAt;
        
        // Initialize status history (create empty if null)
        this.statusHistory = statusHistory == null ? new CustomStack<>() : statusHistory;
        
        // Ensure history has at least one entry (the initial status)
        if (this.statusHistory.isEmpty()) {
            this.statusHistory.push(historyEntry(this.status, this.createdAt));
        }
    }

    // ========== GETTER METHODS ==========
    // These methods provide read-only access to the ticket's data.
    // They follow JavaBean naming conventions (get + property name).

    /** @return the unique ticket identifier */
    public String getId() {
        return id;
    }

    /** @return the student number of the student who submitted this ticket */
    public String getStudentNumber() {
        return studentNumber;
    }

    /** @return the brief subject/title of the ticket */
    public String getSubject() {
        return subject;
    }

    /** @return the detailed description of the issue */
    public String getDescription() {
        return description;
    }

    /** @return the current workflow status of the ticket */
    public String getStatus() {
        return status;
    }

    /** @return the timestamp when this ticket was created */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return the timestamp when this ticket was resolved, or null if still active */
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    /** @return the stack containing the complete status change history */
    public CustomStack<String> getStatusHistory() {
        return statusHistory;
    }

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Updates the ticket status and records the change in history.
     * 
     * This method handles the core workflow of ticket processing:
     * 1. Validates and normalizes the new status
     * 2. Checks if status actually changed (no action if same)
     * 3. Updates the current status
     * 4. Records the change in the history stack
     * 5. Manages resolution timestamp based on status
     * 
     * @param nextStatus The new status to set (will be normalized)
     */
    public void updateStatus(String nextStatus) {
        // Normalize the new status to ensure it's valid
        String normalized = normalizeStatus(nextStatus);
        
        // Don't do anything if status is the same
        if (normalized.equals(this.status)) {
            return;
        }
        
        // Update the current status
        this.status = normalized;
        
        // Record the timestamp for this change
        LocalDateTime now = LocalDateTime.now();
        
        // Add this change to the history stack (newest on top)
        statusHistory.push(historyEntry(normalized, now));
        
        // Handle resolution timestamp
        if (STATUS_CLOSED.equals(normalized)) {
            // Ticket is now resolved - set resolution timestamp
            this.resolvedAt = now;
        } else {
            // Ticket is reopened - clear resolution timestamp
            this.resolvedAt = null;
        }
    }

    // ========== UTILITY METHODS ==========

    /**
     * Validates whether a status value is one of the allowed workflow states.
     * 
     * This is a static method, meaning it can be called without creating a Ticket object.
     * It's useful for input validation throughout the application.
     * 
     * @param value The status string to validate
     * @return true if the status is valid, false otherwise
     */
    public static boolean isValidStatus(String value) {
        if (value == null) {
            return false;
        }
        
        // Normalize for comparison (trim spaces and convert to lowercase)
        String normalized = value.trim().toLowerCase();
        
        // Check against all valid status constants
        return STATUS_OPEN.equals(normalized)
                || STATUS_IN_PROGRESS.equals(normalized)
                || STATUS_CLOSED.equals(normalized);
    }

    /**
     * Converts any status text to the canonical (standard) form.
     * 
     * This method ensures consistency by converting variations like "OPEN", " Open ", 
     * or "open" all to the standard "open" format. Invalid values default to "open".
     * 
     * @param value The status text to normalize
     * @return the normalized status string
     */
    private static String normalizeStatus(String value) {
        if (!isValidStatus(value)) {
            return STATUS_OPEN; // Safe default for invalid values
        }
        return value.trim().toLowerCase();
    }

    /**
     * Creates a human-readable entry for the status history.
     * 
     * This helper method formats a timestamp and status into a readable string
     * that shows when a status change occurred and what the new status was.
     * 
     * @param status The status that was set
     * @param at The timestamp when the status was changed
     * @return Formatted history entry string
     */
    private static String historyEntry(String status, LocalDateTime at) {
        return at.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " -> " + status;
    }

    /**
     * Returns a compact string representation of the ticket for display in menus.
     * 
     * This method overrides the default toString() from the Object class to provide
     * a concise summary suitable for listing tickets in the user interface.
     * 
     * @return Formatted string with key ticket information
     */
    @Override
    public String toString() {
        return id + " | " + subject + " | " + status + " | Student: " + studentNumber;
    }
}
