package model;

import datastructures.CustomArrayList;
import java.time.LocalDate;

/**
 * Represents a campus event.
 * Events can be created by admins and students can register to attend them.
 */
public class Event {
    /** Unique identifier for the event */
    private final String id;
    /** Name of the event */
    private final String name;
    /** Detailed description of the event */
    private final String description;
    /** Date when the event takes place */
    private final LocalDate date;
    /** Physical or virtual location of the event */
    private final String location;
    /** List of student IDs registered for this event */
    private final CustomArrayList<String> attendeeStudentIds;

    /**
     * Constructor for creating a new event.
     *
     * @param id Unique event ID
     * @param name Event name
     * @param description Event description
     * @param date Event date
     * @param location Event location
     */
    public Event(String id, String name, String description, LocalDate date, String location) {
        this(id, name, description, date, location, new CustomArrayList<>());
    }

    /**
     * Full constructor for persistence and detailed initialization.
     *
     * @param id Unique event ID
     * @param name Event name
     * @param description Event description
     * @param date Event date
     * @param location Event location
     * @param attendeeStudentIds List of registered students
     */
    public Event(String id, String name, String description, LocalDate date, String location, 
                 CustomArrayList<String> attendeeStudentIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.location = location;
        this.attendeeStudentIds = attendeeStudentIds != null ? attendeeStudentIds : new CustomArrayList<>();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public String getLocation() { return location; }
    public CustomArrayList<String> getAttendeeStudentIds() { return attendeeStudentIds; }

    /**
     * Registers a student for the event.
     * @param studentId Student number to register
     * @return true if registered, false if already registered
     */
    public boolean registerStudent(String studentId) {
        if (!attendeeStudentIds.contains(studentId)) {
            attendeeStudentIds.add(studentId);
            return true;
        }
        return false;
    }

    /**
     * Cancels a student's registration for the event.
     * @param studentId Student number to unregister
     * @return true if student was found and removed
     */
    public boolean cancelRegistration(String studentId) {
        for (int i = 0; i < attendeeStudentIds.size(); i++) {
            if (attendeeStudentIds.get(i).equals(studentId)) {
                attendeeStudentIds.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s on %s at %s (%d attending)", 
                id, name, date.toString(), location, attendeeStudentIds.size());
    }
}
