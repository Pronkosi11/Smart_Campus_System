package service;

public class EventService {
    private static EventService instance;

    private EventService() {
    }

    public static EventService getInstance() {
        if (instance == null) {
            instance = new EventService();
        }
        return instance;
    }

    public String getAdminStatusMessage() {
        return "Manage Events is not implemented in this phase.";
    }

    public String getStudentStatusMessage() {
        return "Event Booking is not implemented in this phase.";
    }
}
