package service;

public class HostelService {
    private static HostelService instance;

    private HostelService() {
    }

    public static HostelService getInstance() {
        if (instance == null) {
            instance = new HostelService();
        }
        return instance;
    }

    public String getAdminStatusMessage() {
        return "Manage Hostels is not implemented in this phase.";
    }

    public String getStudentStatusMessage() {
        return "Hostel Application is not implemented in this phase.";
    }
}
