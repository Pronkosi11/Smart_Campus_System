package service;

/**
 * Hostel module service placeholder.
 * <p>
 * This class follows the same singleton pattern as other services so it can be
 * wired into `ConsoleUI` now and expanded later without changing integration code.
 */
public class HostelService {
    private static HostelService instance;

    /**
     * Private constructor to enforce singleton usage through getInstance().
     */
    private HostelService() {
    }

    /**
     * Returns the single shared HostelService instance.
     */
    public static HostelService getInstance() {
        if (instance == null) {
            instance = new HostelService();
        }
        return instance;
    }

    /**
     * Admin dashboard message shown when "Manage Hostels" is selected.
     * Explains that the feature is planned but not yet available in this phase.
     */
    public String getAdminStatusMessage() {
        return "Hostel management is planned but not yet available in this phase. "
                + "This menu will support room setup, updates, and allocations.";
    }

    /**
     * Student portal message shown when "Hostel Application" is selected.
     * Explains upcoming capability in beginner-friendly wording.
     */
    public String getStudentStatusMessage() {
        return "Hostel application is planned but not yet available in this phase. "
                + "This menu will allow room applications and application status tracking.";
    }
}
