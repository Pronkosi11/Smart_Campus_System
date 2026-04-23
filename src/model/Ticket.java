package model;

import datastructures.CustomStack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    // Allowed workflow states for help desk processing.
    public static final String STATUS_OPEN = "open";
    public static final String STATUS_IN_PROGRESS = "in-progress";
    public static final String STATUS_CLOSED = "closed";

    // Immutable identity and submission details.
    private final String id;
    private final String studentNumber;
    private final String subject;
    private final String description;

    // Mutable lifecycle details.
    private String status;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    // Stack keeps newest status change on top (LIFO).
    private final CustomStack<String> statusHistory;

    /**
     * Convenience constructor for creating a new ticket from UI input.
     * A new ticket starts with "open" status and current timestamp.
     */
    public Ticket(String id, String studentNumber, String subject, String description) {
        this(id, studentNumber, subject, description, STATUS_OPEN, LocalDateTime.now(), null, new CustomStack<>());
    }

    /**
     * Full constructor used by persistence restore and advanced initialization.
     * It normalizes status values and ensures history starts with at least one entry.
     */
    public Ticket(String id, String studentNumber, String subject, String description, String status,
                  LocalDateTime createdAt, LocalDateTime resolvedAt, CustomStack<String> statusHistory) {
        this.id = id;
        this.studentNumber = studentNumber;
        this.subject = subject;
        this.description = description;
        this.status = normalizeStatus(status);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.resolvedAt = resolvedAt;
        this.statusHistory = statusHistory == null ? new CustomStack<>() : statusHistory;
        if (this.statusHistory.isEmpty()) {
            this.statusHistory.push(historyEntry(this.status, this.createdAt));
        }
    }

    /**
     * Returns the ticket reference ID (e.g., TKT-1001).
     */
    public String getId() {
        return id;
    }

    /**
     * Returns student number of ticket owner.
     */
    public String getStudentNumber() {
        return studentNumber;
    }

    /**
     * Returns short issue title provided by the student.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Returns full issue description submitted by the student.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns current workflow status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns timestamp when ticket was created.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns close timestamp, or null when still active.
     */
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    /**
     * Returns the underlying status history stack.
     */
    public CustomStack<String> getStatusHistory() {
        return statusHistory;
    }

    /**
     * Moves ticket to a new status and records the transition in history.
     * If moved to closed, resolvedAt is set; otherwise resolvedAt is cleared.
     */
    public void updateStatus(String nextStatus) {
        String normalized = normalizeStatus(nextStatus);
        if (normalized.equals(this.status)) {
            return;
        }
        this.status = normalized;
        LocalDateTime now = LocalDateTime.now();
        statusHistory.push(historyEntry(normalized, now));
        if (STATUS_CLOSED.equals(normalized)) {
            this.resolvedAt = now;
        } else {
            this.resolvedAt = null;
        }
    }

    /**
     * Validates that a status belongs to the supported workflow.
     */
    public static boolean isValidStatus(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return STATUS_OPEN.equals(normalized)
                || STATUS_IN_PROGRESS.equals(normalized)
                || STATUS_CLOSED.equals(normalized);
    }

    /**
     * Converts user or persisted status text to canonical form.
     * Unknown values safely fall back to "open".
     */
    private static String normalizeStatus(String value) {
        if (!isValidStatus(value)) {
            return STATUS_OPEN;
        }
        return value.trim().toLowerCase();
    }

    /**
     * Builds one human-readable history row: "<timestamp> -> <status>".
     */
    private static String historyEntry(String status, LocalDateTime at) {
        return at.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " -> " + status;
    }

    /**
     * Compact text representation used in menu lists.
     */
    @Override
    public String toString() {
        return id + " | " + subject + " | " + status + " | Student: " + studentNumber;
    }
}
