package model;

import datastructures.CustomArrayList;

/**
 * HostelRoom - Hostel/Residence Entity Model
 *
 * Represents one campus residence (hostel) where students can live.
 * The system uses these records to:
 *   1. Show students which residences they may apply for (based on
 *      gender, year of study and other allocation rules), and
 *   2. Track which students are currently allocated to each residence.
 *
 * Why this design?
 * ----------------
 *  - {@link CustomArrayList} is used for the occupants list because we
 *    only ever need to add, remove and iterate over student numbers —
 *    a dynamic list does this perfectly.
 *  - "capacity" is a simple integer cap so the service can quickly
 *    answer the question "is this hostel full?".
 *  - "allocationGroup" is a string label (e.g. "Females Undergraduates")
 *    that the service uses to filter who is eligible to apply.
 *
 * Beginner notes:
 *  - This class deliberately does NOT decide who is eligible — that is
 *    the service's job. This keeps the model dumb and reusable.
 *  - All fields stay PRIVATE. Use the methods to read or change them.
 */
public class HostelRoom {

    // ========== HOSTEL IDENTITY ==========
    // Display name shown in menus, e.g. "Oliver Tambo".
    private String name;
    // Optional short code, e.g. "MBA". May be null in the JSON file.
    private String code;
    // Optional unit/floor range, e.g. "0-14". May be null.
    private String unitRange;
    // Allocation policy label that controls which students may apply,
    // e.g. "Males First Entering" or "Females Undergraduates".
    private String allocationGroup;

    // ========== CAPACITY AND OCCUPANTS ==========
    // Maximum number of students that can live in this hostel.
    private int capacity;
    // Student numbers of students currently allocated to this hostel.
    private CustomArrayList<String> occupants;

    /**
     * Builds a hostel record. New hostels start empty (no occupants).
     *
     * @param name            display name (must not be null)
     * @param code            optional short code (may be null)
     * @param unitRange       optional unit/floor range (may be null)
     * @param allocationGroup eligibility group label (must not be null)
     * @param capacity        how many students fit in this hostel
     */
    public HostelRoom(String name, String code, String unitRange,
                      String allocationGroup, int capacity) {
        this.name = name;
        this.code = code;
        this.unitRange = unitRange;
        this.allocationGroup = allocationGroup;
        // Defensive guard: capacity must not be negative.
        this.capacity = Math.max(0, capacity);
        this.occupants = new CustomArrayList<>();
    }

    // ========== GETTERS AND SETTERS ==========

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getUnitRange() { return unitRange; }
    public void setUnitRange(String unitRange) { this.unitRange = unitRange; }

    public String getAllocationGroup() { return allocationGroup; }
    public void setAllocationGroup(String allocationGroup) { this.allocationGroup = allocationGroup; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = Math.max(0, capacity); }

    public CustomArrayList<String> getOccupants() { return occupants; }

    /**
     * Used by the persistence layer to restore the occupants list
     * exactly as it was saved last time.
     */
    public void setOccupants(CustomArrayList<String> occupants) {
        this.occupants = (occupants != null) ? occupants : new CustomArrayList<>();
    }

    // ========== ALLOCATION HELPER METHODS ==========

    /**
     * Quick check: is the hostel full?
     */
    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    /**
     * Adds a student to this hostel if there is space and they are
     * not already allocated here. Returns true on success.
     *
     * @param studentNumber student to allocate
     */
    public boolean allocateStudent(String studentNumber) {
        if (studentNumber == null || studentNumber.isBlank()) {
            return false;
        }
        if (isFull() || occupants.contains(studentNumber)) {
            return false;
        }
        occupants.add(studentNumber);
        return true;
    }

    /**
     * Removes a student from this hostel. Returns true if they were
     * actually allocated here (and have now been removed).
     */
    public boolean removeStudent(String studentNumber) {
        return occupants.remove(studentNumber);
    }

    /**
     * Concise display string used in menu listings.
     */
    @Override
    public String toString() {
        // Show "(code)" only when a code is present, otherwise omit it.
        String codePart = (code == null || code.isBlank()) ? "" : " (" + code + ")";
        return String.format("%s%s | %s | %d/%d filled",
                name, codePart, allocationGroup, occupants.size(), capacity);
    }
}
