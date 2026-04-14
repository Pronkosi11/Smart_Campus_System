package service;

public class HelpDeskModuleService {
    private static HelpDeskModuleService instance;

    private HelpDeskModuleService() {
    }

    public static HelpDeskModuleService getInstance() {
        if (instance == null) {
            instance = new HelpDeskModuleService();
        }
        return instance;
    }

    public String getStatusMessage() {
        return "View Help Desk Tickets is not implemented in this phase.";
    }
}
