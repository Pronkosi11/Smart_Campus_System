package service;

import datastructures.CustomArrayList;
import model.Event;
import model.Student;
import persistence.DataPersistence;
import ui.BoxUI;
import java.time.LocalDate;

/**
 * Service class for event management.
 * Handles event creation for admins and registration for students.
 */
public class EventService {
    private static EventService instance;
    /** In-memory storage for events */
    private CustomArrayList<Event> events = new CustomArrayList<>();

    /**
     * Private constructor for Singleton pattern.
     */
    private EventService() {
    }

    /**
     * Returns the singleton instance of EventService.
     */
    public static EventService getInstance() {
        if (instance == null) {
            instance = new EventService();
        }
        return instance;
    }

    /**
     * Sets the event data loaded from persistence.
     * @param loadedEvents List of events
     */
    public void setEvents(CustomArrayList<Event> loadedEvents) {
        this.events = loadedEvents;
    }

    /**
     * Returns the internal list of events for persistence saving.
     */
    public CustomArrayList<Event> getEventsList() {
        return events;
    }

    /**
     * Displays the administrative event menu.
     * @param box UI utility
     */
    public void showAdminEventMenu(BoxUI box) {
        int choice;
        do {
            box.printMenu("EVENT MANAGEMENT (ADMIN)", new String[]{
                    "1. Create New Event",
                    "2. Cancel Event",
                    "3. List All Events",
                    "4. Back to Main Menu"
            });
            choice = box.readInt("Choose option: ", 1, 4);
            switch (choice) {
                case 1: createEventFlow(box); break;
                case 2: cancelEventFlow(box); break;
                case 3: listAllEvents(box); break;
            }
        } while (choice != 4);
    }

    /**
     * Displays the student event portal.
     * @param box UI utility
     * @param student Currently logged-in student
     */
    public void showStudentEventMenu(BoxUI box, Student student) {
        int choice;
        do {
            box.printMenu("EVENT BOOKING", new String[]{
                    "1. View Upcoming Events",
                    "2. Register for Event",
                    "3. Cancel Registration",
                    "4. View My Registered Events",
                    "5. Back to Portal"
            });
            choice = box.readInt("Choose option: ", 1, 5);
            switch (choice) {
                case 1: listUpcomingEvents(box); break;
                case 2: registerEventFlow(box, student); break;
                case 3: cancelRegistrationFlow(box, student); break;
                case 4: viewMyEvents(box, student); break;
            }
        } while (choice != 5);
    }

    // --- Admin Operations ---

    private void createEventFlow(BoxUI box) {
        String id = box.prompt("Enter Event ID (e.g., EV-101): ");
        if (findEventById(id) != null) {
            box.error("Event with ID " + id + " already exists.");
            return;
        }
        String name = box.prompt("Enter Event Name: ");
        String desc = box.prompt("Enter Description: ");
        String dateStr = box.prompt("Enter Date (YYYY-MM-DD): ");
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception e) {
            box.error("Invalid date format. Use YYYY-MM-DD.");
            return;
        }
        String loc = box.prompt("Enter Location: ");

        Event event = new Event(id, name, desc, date, loc);
        events.add(event);
        DataPersistence.saveEvents(events);
        box.success("Event created successfully.");
    }

    private void cancelEventFlow(BoxUI box) {
        String id = box.prompt("Enter Event ID to cancel: ");
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(id)) {
                events.remove(i);
                DataPersistence.saveEvents(events);
                box.success("Event cancelled and removed.");
                return;
            }
        }
        box.error("Event not found.");
    }

    // --- Student Operations ---

    private void registerEventFlow(BoxUI box, Student student) {
        String id = box.prompt("Enter Event ID to register: ");
        Event e = findEventById(id);
        if (e == null) {
            box.error("Event not found.");
            return;
        }

        if (e.registerStudent(student.getStudentNumber())) {
            DataPersistence.saveEvents(events);
            box.success("Successfully registered for " + e.getName());
        } else {
            box.error("You are already registered for this event.");
        }
    }

    private void cancelRegistrationFlow(BoxUI box, Student student) {
        String id = box.prompt("Enter Event ID to cancel registration: ");
        Event e = findEventById(id);
        if (e == null) {
            box.error("Event not found.");
            return;
        }

        if (e.cancelRegistration(student.getStudentNumber())) {
            DataPersistence.saveEvents(events);
            box.success("Registration cancelled.");
        } else {
            box.error("You were not registered for this event.");
        }
    }

    private void viewMyEvents(BoxUI box, Student student) {
        CustomArrayList<String> linesList = new CustomArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            if (e.getAttendeeStudentIds().contains(student.getStudentNumber())) {
                linesList.add(e.toString());
            }
        }
        
        if (linesList.isEmpty()) {
            box.info("You have not registered for any events.");
            return;
        }

        String[] lines = new String[linesList.size()];
        for (int i = 0; i < linesList.size(); i++) {
            lines[i] = linesList.get(i);
        }

        box.printSection("My Registered Events", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    // --- Common ---

    private void listAllEvents(BoxUI box) {
        if (events.isEmpty()) {
            box.info("No events registered.");
            return;
        }
        String[] lines = new String[events.size()];
        for (int i = 0; i < events.size(); i++) {
            lines[i] = events.get(i).toString();
        }
        box.printSection("All Campus Events", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    private void listUpcomingEvents(BoxUI box) {
        LocalDate today = LocalDate.now();
        CustomArrayList<String> linesList = new CustomArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            if (!e.getDate().isBefore(today)) {
                linesList.add(e.toString());
            }
        }

        if (linesList.isEmpty()) {
            box.info("No upcoming events found.");
            return;
        }

        String[] lines = new String[linesList.size()];
        for (int i = 0; i < linesList.size(); i++) {
            lines[i] = linesList.get(i);
        }

        box.printSection("Upcoming Events", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    private Event findEventById(String id) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(id)) {
                return events.get(i);
            }
        }
        return null;
    }
}
