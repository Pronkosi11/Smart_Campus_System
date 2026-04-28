package service;

import datastructures.CustomArrayList;
import datastructures.CustomHashMap;
import model.Event;
import model.Student;
import persistence.DataPersistence;
import ui.BoxUI;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * EventService - Event Module Business Logic
 *
 * Manages campus events. Admin can create / list / remove events.
 * Students can browse events, register to attend, and cancel their
 * registration.
 *
 * Why these data structures?
 * --------------------------
 *  - {@link CustomHashMap} keyed by eventId for O(1) lookup.
 *  - Each event keeps its registered-students list as a
 *    {@link CustomArrayList}, which is perfect for add/remove/iterate.
 *
 * Beginner notes:
 *  - Date input is parsed with {@link DateTimeFormatter}. We use the
 *    standard ISO format ("yyyy-MM-ddTHH:mm") because it is the same
 *    format Java prints by default — round-trip safe.
 *  - All errors are reported through BoxUI so the user always gets
 *    a clear message instead of a stack trace.
 */
public class EventService {

    // The format we accept and display for event date+time fields.
    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ---- Singleton plumbing ----
    private static EventService instance;

    // Master catalogue: eventId -> Event.
    private CustomHashMap<String, Event> events;

    private EventService() {
        this.events = new CustomHashMap<>();
    }

    public static EventService getInstance() {
        if (instance == null) {
            instance = new EventService();
        }
        return instance;
    }

    // ============================================================
    //  PERSISTENCE INTEGRATION
    // ============================================================

    public void setEvents(CustomHashMap<String, Event> loaded) {
        this.events = (loaded != null) ? loaded : new CustomHashMap<>();
    }

    public CustomHashMap<String, Event> getEventsMap() {
        return events;
    }

    // ============================================================
    //  CORE OPERATIONS
    // ============================================================

    public void addEvent(Event event) {
        events.put(event.getEventId(), event);
        DataPersistence.saveEvents(events);
    }

    public boolean removeEvent(String eventId) {
        Event removed = events.remove(eventId);
        if (removed != null) {
            DataPersistence.saveEvents(events);
            return true;
        }
        return false;
    }

    public Event getEvent(String eventId) {
        return events.get(eventId);
    }

    public CustomArrayList<Event> getAllEvents() {
        return events.values();
    }

    /**
     * Result codes for student registration attempts. The UI maps
     * each one to a friendly message.
     */
    public enum RegisterResult {
        SUCCESS, ALREADY_REGISTERED, EVENT_FULL, EVENT_NOT_FOUND
    }

    public RegisterResult registerStudent(String studentNumber, String eventId) {
        Event event = events.get(eventId);
        if (event == null) {
            return RegisterResult.EVENT_NOT_FOUND;
        }
        if (event.isStudentRegistered(studentNumber)) {
            return RegisterResult.ALREADY_REGISTERED;
        }
        if (event.isFull()) {
            return RegisterResult.EVENT_FULL;
        }
        event.registerStudent(studentNumber);
        DataPersistence.saveEvents(events);
        return RegisterResult.SUCCESS;
    }

    /**
     * Cancels a student's registration. Returns true if the student
     * was registered and has now been removed.
     */
    public boolean cancelRegistration(String studentNumber, String eventId) {
        Event event = events.get(eventId);
        if (event == null) {
            return false;
        }
        boolean removed = event.cancelRegistration(studentNumber);
        if (removed) {
            DataPersistence.saveEvents(events);
        }
        return removed;
    }

    /**
     * Returns every event a single student has registered for.
     */
    public CustomArrayList<Event> getEventsForStudent(String studentNumber) {
        CustomArrayList<Event> mine = new CustomArrayList<>();
        CustomArrayList<Event> all = events.values();
        for (int i = 0; i < all.size(); i++) {
            Event e = all.get(i);
            if (e.isStudentRegistered(studentNumber)) {
                mine.add(e);
            }
        }
        return mine;
    }

    // ============================================================
    //  ADMIN MENU
    // ============================================================

    public void showAdminEventsMenu(BoxUI box) {
        int choice;
        do {
            box.printMenu("MANAGE EVENTS", new String[]{
                    "1. List All Events",
                    "2. Add New Event",
                    "3. Remove Event",
                    "4. View Attendees for an Event",
                    "5. Back"
            });
            choice = box.readInt("Choose option: ", 1, 5);
            switch (choice) {
                case 1: listEvents(box); break;
                case 2: addEventFlow(box); break;
                case 3: removeEventFlow(box); break;
                case 4: viewAttendeesFlow(box); break;
                case 5: break;
                default: box.error("Invalid option.");
            }
        } while (choice != 5);
    }

    // ============================================================
    //  STUDENT MENU
    // ============================================================

    public void showStudentEventsMenu(BoxUI box, Student student) {
        int choice;
        do {
            box.printMenu("EVENT BOOKING", new String[]{
                    "1. Browse All Events",
                    "2. Register for an Event",
                    "3. Cancel My Registration",
                    "4. View My Registered Events",
                    "5. Back"
            });
            choice = box.readInt("Choose option: ", 1, 5);
            switch (choice) {
                case 1: listEvents(box); break;
                case 2: registerFlow(box, student); break;
                case 3: cancelFlow(box, student); break;
                case 4: listMyEvents(box, student); break;
                case 5: break;
                default: box.error("Invalid option.");
            }
        } while (choice != 5);
    }

    // ============================================================
    //  UI FLOW HELPERS
    // ============================================================

    private void listEvents(BoxUI box) {
        CustomArrayList<Event> all = getAllEvents();
        if (all.isEmpty()) { box.info("No events scheduled."); return; }
        box.printSection("CAMPUS EVENTS");
        for (int i = 0; i < all.size(); i++) {
            box.line((i + 1) + ". " + all.get(i));
        }
        box.endSection();
    }

    private void addEventFlow(BoxUI box) {
        String id = box.prompt("Event ID (e.g. E003): ");
        if (id.isEmpty()) { box.error("Event ID is required."); return; }
        if (events.get(id) != null) { box.error("An event with that ID already exists."); return; }

        String name = box.prompt("Event name: ");
        if (name.isEmpty()) { box.error("Event name is required."); return; }
        String description = box.prompt("Description: ");
        String venue = box.prompt("Venue: ");
        if (venue.isEmpty()) { box.error("Venue is required."); return; }

        // Date/time input — we keep it simple by asking for a known format.
        LocalDateTime start = promptDateTime(box, "Start (YYYY-MM-DD HH:MM): ");
        if (start == null) { box.error("Could not understand the start date/time. Please try again."); return; }
        LocalDateTime end = promptDateTime(box, "End   (YYYY-MM-DD HH:MM): ");
        if (end == null) { box.error("Could not understand the end date/time. Please try again."); return; }
        if (end.isBefore(start)) { box.error("End time must be after the start time."); return; }

        int max = box.readInt("Max participants (1-1000): ", 1, 1000);
        String organizer = box.prompt("Organizer: ");
        if (organizer.isEmpty()) { organizer = "Campus"; }

        addEvent(new Event(id, name, description, venue, start, end, max, organizer));
        box.success("Event added.");
    }

    private void removeEventFlow(BoxUI box) {
        String id = box.prompt("Event ID to remove: ");
        if (removeEvent(id)) {
            box.success("Event removed.");
        } else {
            box.error("No event found with that ID.");
        }
    }

    private void viewAttendeesFlow(BoxUI box) {
        String id = box.prompt("Event ID: ");
        Event e = events.get(id);
        if (e == null) { box.error("No event found with that ID."); return; }

        CustomArrayList<String> attendees = e.getRegisteredStudents();
        if (attendees.isEmpty()) {
            box.info("No students have registered for this event.");
            return;
        }
        box.printSection("ATTENDEES - " + e.getEventName());
        for (int i = 0; i < attendees.size(); i++) {
            box.line((i + 1) + ". " + attendees.get(i));
        }
        box.endSection();
    }

    private void registerFlow(BoxUI box, Student student) {
        String id = box.prompt("Event ID to register for: ");
        RegisterResult result = registerStudent(student.getStudentNumber(), id);
        switch (result) {
            case SUCCESS:            box.success("Registered. See you there!"); break;
            case ALREADY_REGISTERED: box.error("You are already registered for this event."); break;
            case EVENT_FULL:         box.error("Sorry, this event is full."); break;
            case EVENT_NOT_FOUND:    box.error("No event found with that ID."); break;
        }
    }

    private void cancelFlow(BoxUI box, Student student) {
        String id = box.prompt("Event ID to cancel: ");
        if (cancelRegistration(student.getStudentNumber(), id)) {
            box.success("Registration cancelled.");
        } else {
            box.error("You are not registered for that event.");
        }
    }

    private void listMyEvents(BoxUI box, Student student) {
        CustomArrayList<Event> mine = getEventsForStudent(student.getStudentNumber());
        if (mine.isEmpty()) { box.info("You have not registered for any events."); return; }
        box.printSection("MY REGISTERED EVENTS");
        for (int i = 0; i < mine.size(); i++) {
            box.line((i + 1) + ". " + mine.get(i));
        }
        box.endSection();
    }

    // ============================================================
    //  PRIVATE UTILITIES
    // ============================================================

    /**
     * Prompts the user for a date/time and parses it. Returns null if
     * the user typed something we cannot understand — the caller can
     * then show an error and ask again.
     */
    private LocalDateTime promptDateTime(BoxUI box, String prompt) {
        String text = box.prompt(prompt);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
