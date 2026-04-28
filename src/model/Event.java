package model;

import datastructures.CustomArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Event - Campus Event Entity Model
 *
 * Represents a single campus event (e.g. a hackathon, careers fair,
 * sports day) that students can register to attend.
 *
 * Why these data structures?
 * --------------------------
 *  - {@link CustomArrayList} for registered students because we need
 *    to add new sign-ups, remove cancellations and iterate the list
 *    to print attendees. A dynamic list does all three efficiently.
 *  - "maxParticipants" is just an integer cap so we can quickly check
 *    whether the event is full.
 *
 * Beginner notes:
 *  - Dates are stored using {@link LocalDateTime} (the modern Java 8+
 *    way to handle a date AND a time together).
 *  - The {@code DateTimeFormatter} is shared as a constant so we never
 *    re-create it each time we print — that is also good practice.
 */
public class Event {

    // Format used everywhere the event date is shown to the user.
    // Example output: "2026-05-15 09:00".
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ========== EVENT IDENTITY ==========
    private String eventId;       // Unique id, e.g. "E001".
    private String eventName;     // Display name, e.g. "Campus Hackathon 2026".
    private String description;   // Longer description shown in detail views.
    private String venue;         // Where the event happens, e.g. "Great Hall".
    private String organizer;     // Name of the organising society/department.

    // ========== SCHEDULE ==========
    private LocalDateTime startDateTime; // When the event starts.
    private LocalDateTime endDateTime;   // When the event ends.

    // ========== CAPACITY AND ATTENDEES ==========
    // Maximum number of students that may sign up.
    private int maxParticipants;
    // Student numbers of students who have registered to attend.
    private CustomArrayList<String> registeredStudents;

    /**
     * Builds a new event. The list of registered students starts empty.
     *
     * @param eventId         unique identifier (e.g. "E001")
     * @param eventName       display name shown in menus
     * @param description     full description for the detail view
     * @param venue           where it takes place
     * @param startDateTime   start time (must not be null)
     * @param endDateTime     end time (must not be null)
     * @param maxParticipants capacity cap (must be at least 1)
     * @param organizer       organising body
     */
    public Event(String eventId, String eventName, String description, String venue,
                 LocalDateTime startDateTime, LocalDateTime endDateTime,
                 int maxParticipants, String organizer) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.venue = venue;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        // Defensive guard: must allow at least one participant.
        this.maxParticipants = Math.max(1, maxParticipants);
        this.organizer = organizer;
        this.registeredStudents = new CustomArrayList<>();
    }

    // ========== GETTERS AND SETTERS ==========

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = Math.max(1, maxParticipants); }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public CustomArrayList<String> getRegisteredStudents() { return registeredStudents; }

    /**
     * Used by the persistence layer to restore the attendee list after
     * loading the JSON file.
     */
    public void setRegisteredStudents(CustomArrayList<String> registeredStudents) {
        this.registeredStudents = (registeredStudents != null) ? registeredStudents : new CustomArrayList<>();
    }

    // ========== REGISTRATION HELPER METHODS ==========

    /**
     * Quick check: is the event already at capacity?
     */
    public boolean isFull() {
        return registeredStudents.size() >= maxParticipants;
    }

    /**
     * Quick check: has this student already signed up?
     */
    public boolean isStudentRegistered(String studentNumber) {
        return registeredStudents.contains(studentNumber);
    }

    /**
     * Adds a student to the attendee list.
     * Returns true on success, false if the event is full or the
     * student is already registered.
     */
    public boolean registerStudent(String studentNumber) {
        if (studentNumber == null || studentNumber.isBlank()) {
            return false;
        }
        if (isFull() || isStudentRegistered(studentNumber)) {
            return false;
        }
        registeredStudents.add(studentNumber);
        return true;
    }

    /**
     * Removes a student from the attendee list.
     * Returns true if the student was actually registered.
     */
    public boolean cancelRegistration(String studentNumber) {
        return registeredStudents.remove(studentNumber);
    }

    /**
     * Concise display string used in menu listings, e.g.
     * "E001 - Campus Hackathon 2026 @ Innovation Lab | 2026-05-15 09:00 [1/80]".
     */
    @Override
    public String toString() {
        return String.format("%s - %s @ %s | %s [%d/%d]",
                eventId, eventName, venue,
                startDateTime.format(DISPLAY_FORMAT),
                registeredStudents.size(), maxParticipants);
    }
}
