package model;

import datastructures.CustomStack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    public static final String STATUS_OPEN = "open";
    public static final String STATUS_IN_PROGRESS = "in-progress";
    public static final String STATUS_CLOSED = "closed";

    private final String id;
    private final String studentNumber;
    private final String subject;
    private final String description;
    private String status;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private final CustomStack<String> statusHistory;

    public Ticket(String id, String studentNumber, String subject, String description) {
        this(id, studentNumber, subject, description, STATUS_OPEN, LocalDateTime.now(), null, new CustomStack<>());
    }

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

    public String getId() {
        return id;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public CustomStack<String> getStatusHistory() {
        return statusHistory;
    }

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

    public static boolean isValidStatus(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return STATUS_OPEN.equals(normalized)
                || STATUS_IN_PROGRESS.equals(normalized)
                || STATUS_CLOSED.equals(normalized);
    }

    private static String normalizeStatus(String value) {
        if (!isValidStatus(value)) {
            return STATUS_OPEN;
        }
        return value.trim().toLowerCase();
    }

    private static String historyEntry(String status, LocalDateTime at) {
        return at.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " -> " + status;
    }

    @Override
    public String toString() {
        return id + " | " + subject + " | " + status + " | Student: " + studentNumber;
    }
}
