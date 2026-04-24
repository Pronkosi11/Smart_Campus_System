package model;

import datastructures.CustomArrayList;
import java.util.Objects;

/**
 * Represents a hostel room in the campus.
 * Manages room details, capacity, and current occupants.
 */
public class HostelRoom {
    /** Unique identifier for the room (e.g., HR-101) */
    private final String id;
    /** Human-readable room number (e.g., "Room 101") */
    private final String roomNumber;
    /** Maximum number of students that can stay in this room */
    private final int capacity;
    /** List of student IDs currently assigned to this room */
    private final CustomArrayList<String> occupantStudentIds;
    /** Status indicating if the room can accept more students */
    private boolean isAvailable;

    /**
     * Constructor for creating a new hostel room.
     * Initially empty and available if capacity > 0.
     *
     * @param id Unique room ID
     * @param roomNumber Room name/number
     * @param capacity Maximum occupancy
     */
    public HostelRoom(String id, String roomNumber, int capacity) {
        this(id, roomNumber, capacity, new CustomArrayList<>(), capacity > 0);
    }

    /**
     * Full constructor for persistence and detailed initialization.
     *
     * @param id Unique room ID
     * @param roomNumber Room name/number
     * @param capacity Maximum occupancy
     * @param occupantStudentIds Current occupants
     * @param isAvailable Current availability
     */
    public HostelRoom(String id, String roomNumber, int capacity, 
                      CustomArrayList<String> occupantStudentIds, boolean isAvailable) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.occupantStudentIds = occupantStudentIds != null ? occupantStudentIds : new CustomArrayList<>();
        this.isAvailable = isAvailable;
        updateAvailability();
    }

    // Getters
    public String getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public int getCapacity() { return capacity; }
    public CustomArrayList<String> getOccupantStudentIds() { return occupantStudentIds; }
    public boolean isAvailable() { return isAvailable; }

    /**
     * Adds a student to the room if there is space.
     * @param studentId Student number to add
     * @return true if added, false if room is full
     */
    public boolean addOccupant(String studentId) {
        if (occupantStudentIds.size() >= capacity) {
            return false;
        }
        if (!occupantStudentIds.contains(studentId)) {
            occupantStudentIds.add(studentId);
            updateAvailability();
        }
        return true;
    }

    /**
     * Removes a student from the room.
     * @param studentId Student number to remove
     * @return true if student was found and removed
     */
    public boolean removeOccupant(String studentId) {
        for (int i = 0; i < occupantStudentIds.size(); i++) {
            if (occupantStudentIds.get(i).equals(studentId)) {
                occupantStudentIds.remove(i);
                updateAvailability();
                return true;
            }
        }
        return false;
    }

    /**
     * Internal helper to update availability status based on current occupants.
     */
    private void updateAvailability() {
        this.isAvailable = occupantStudentIds.size() < capacity;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Capacity: %d/%d | %s", 
                id, roomNumber, occupantStudentIds.size(), capacity, 
                isAvailable ? "Available" : "Full");
    }
}
