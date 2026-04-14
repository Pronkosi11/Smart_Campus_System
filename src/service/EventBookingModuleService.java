package service;

public class EventBookingModuleService {
    private static EventBookingModuleService instance;

    private EventBookingModuleService() {
    }

    public static EventBookingModuleService getInstance() {
        if (instance == null) {
            instance = new EventBookingModuleService();
        }
        return instance;
    }

    public String getStatusMessage() {
        return "Manage Events is not implemented in this phase.";
    }
}
