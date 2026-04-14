package service;

public class LibraryModuleService {
    private static LibraryModuleService instance;

    private LibraryModuleService() {
    }

    public static LibraryModuleService getInstance() {
        if (instance == null) {
            instance = new LibraryModuleService();
        }
        return instance;
    }

    public String getStatusMessage() {
        return "Manage Library is not implemented in this phase.";
    }
}
