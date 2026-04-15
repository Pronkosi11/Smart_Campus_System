package service;

public class LibraryService {
    private static LibraryService instance;

    private LibraryService() {
    }

    public static LibraryService getInstance() {
        if (instance == null) {
            instance = new LibraryService();
        }
        return instance;
    }

    public String getAdminStatusMessage() {
        return "Manage Library is not implemented in this phase.";
    }

    public String getStudentStatusMessage() {
        return "Library Services is not implemented in this phase.";
    }
}
