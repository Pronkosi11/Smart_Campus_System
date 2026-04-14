package service;

public class HostelModuleService {
    private static HostelModuleService instance;

    private HostelModuleService() {
    }

    public static HostelModuleService getInstance() {
        if (instance == null) {
            instance = new HostelModuleService();
        }
        return instance;
    }

    public String getStatusMessage() {
        return "Manage Hostels is not implemented in this phase.";
    }
}
