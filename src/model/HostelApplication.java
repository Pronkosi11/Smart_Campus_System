package model;

import java.time.LocalDateTime;

/**
 * HostelApplication - One Student's Application For A Hostel
 *
 * A small value object that records "Student X applied for Hostel Y at
 * time T, current status is Z". The HostelService keeps a queue of
 * pending applications and processes them in the order they were
 * submitted (FIFO).
 *
 * Statuses used in this system:
 *   - PENDING  : waiting for an admin decision
 *   - APPROVED : admin allocated the student to the hostel
 *   - REJECTED : admin rejected the application
 *
 * Beginner notes:
 *  - Constants make the code safer than typing the same string in many
 *    places — typos become compile-time errors instead of silent bugs.
 *  - LocalDateTime is the modern Java 8+ way to store a date and time.
 */
public class HostelApplication {

    // ---- Status constants (use these instead of raw strings) ----
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    // ---- Fields ----
    // Student number of the applicant (e.g. "202356843").
    private final String studentNumber;
    // Name of the hostel they want to live in.
    private final String hostelName;
    // Current decision state (see STATUS_* constants above).
    private String status;
    // When the application was submitted (set automatically on creation).
    private final LocalDateTime appliedAt;

    /**
     * Builds a new pending application stamped with the current time.
     */
    public HostelApplication(String studentNumber, String hostelName) {
        this.studentNumber = studentNumber;
        this.hostelName = hostelName;
        this.status = STATUS_PENDING;
        this.appliedAt = LocalDateTime.now();
    }

    /**
     * Constructor used by the persistence layer to restore an existing
     * application from the JSON file. We need the original timestamp
     * and status, not new ones.
     */
    public HostelApplication(String studentNumber, String hostelName,
                             String status, LocalDateTime appliedAt) {
        this.studentNumber = studentNumber;
        this.hostelName = hostelName;
        this.status = (status == null) ? STATUS_PENDING : status;
        this.appliedAt = (appliedAt == null) ? LocalDateTime.now() : appliedAt;
    }

    // ---- Getters / setters ----

    public String getStudentNumber() { return studentNumber; }
    public String getHostelName() { return hostelName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }

    /**
     * Human-friendly summary used in admin and student listings.
     */
    @Override
    public String toString() {
        return studentNumber + " -> " + hostelName + " [" + status + "] at " + appliedAt;
    }
}
