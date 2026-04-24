package service;

import datastructures.CustomArrayList;
import datastructures.CustomHashMap;
import model.HostelRoom;
import model.Student;
import persistence.DataPersistence;
import ui.BoxUI;

/**
 * Service class for hostel management.
 * Handles room creation/removal for admins and applications for students.
 */
public class HostelService {
    private static HostelService instance;
    /** In-memory storage for hostel rooms, keyed by room ID */
    private CustomHashMap<String, HostelRoom> hostels = new CustomHashMap<>();

    /**
     * Private constructor for Singleton pattern.
     */
    private HostelService() {
    }

    /**
     * Returns the singleton instance of HostelService.
     */
    public static HostelService getInstance() {
        if (instance == null) {
            instance = new HostelService();
        }
        return instance;
    }

    /**
     * Sets the hostel data loaded from persistence.
     * @param loadedHostels HashMap of rooms
     */
    public void setHostels(CustomHashMap<String, HostelRoom> loadedHostels) {
        this.hostels = loadedHostels;
    }

    /**
     * Returns the internal map of hostels for persistence saving.
     */
    public CustomHashMap<String, HostelRoom> getHostelsMap() {
        return hostels;
    }

    /**
     * Displays the administrative hostel menu.
     * @param box UI utility
     */
    public void showAdminHostelMenu(BoxUI box) {
        int choice;
        do {
            box.printMenu("HOSTEL MANAGEMENT (ADMIN)", new String[]{
                    "1. Add New Room",
                    "2. Remove Room",
                    "3. List All Rooms",
                    "4. Back to Main Menu"
            });
            choice = box.readInt("Choose option: ", 1, 4);
            switch (choice) {
                case 1: addRoomFlow(box); break;
                case 2: removeRoomFlow(box); break;
                case 3: listAllRooms(box); break;
            }
        } while (choice != 4);
    }

    /**
     * Displays the student hostel portal.
     * @param box UI utility
     * @param student Currently logged-in student
     */
    public void showStudentHostelMenu(BoxUI box, Student student) {
        int choice;
        do {
            box.printMenu("HOSTEL SERVICES", new String[]{
                    "1. View Available Rooms",
                    "2. Apply for Room",
                    "3. Vacate My Room",
                    "4. View My Room Details",
                    "5. Back to Portal"
            });
            choice = box.readInt("Choose option: ", 1, 5);
            switch (choice) {
                case 1: listAvailableRooms(box); break;
                case 2: applyRoomFlow(box, student); break;
                case 3: vacateRoomFlow(box, student); break;
                case 4: viewMyRoom(box, student); break;
            }
        } while (choice != 5);
    }

    // --- Admin Operations ---

    private void addRoomFlow(BoxUI box) {
        String id = box.prompt("Enter Room ID (e.g., HR-101): ");
        if (hostels.containsKey(id)) {
            box.error("Room with ID " + id + " already exists.");
            return;
        }
        String roomNumber = box.prompt("Enter Room Number/Name: ");
        int capacity = box.readInt("Enter Capacity (1-4): ", 1, 4);

        HostelRoom room = new HostelRoom(id, roomNumber, capacity);
        hostels.put(id, room);
        DataPersistence.saveHostels(hostels);
        box.success("Room added successfully.");
    }

    private void removeRoomFlow(BoxUI box) {
        String id = box.prompt("Enter Room ID to remove: ");
        HostelRoom r = hostels.get(id);
        if (r == null) {
            box.error("Room not found.");
            return;
        }
        if (!r.getOccupantStudentIds().isEmpty()) {
            box.error("Cannot remove room with active occupants. Vacate students first.");
            return;
        }
        hostels.remove(id);
        DataPersistence.saveHostels(hostels);
        box.success("Room removed.");
    }

    // --- Student Operations ---

    private void applyRoomFlow(BoxUI box, Student student) {
        if (student.getHostelRoom() != null) {
            box.error("You already have an assigned room: " + student.getHostelRoom());
            return;
        }

        String id = box.prompt("Enter Room ID to apply: ");
        HostelRoom r = hostels.get(id);
        if (r == null) {
            box.error("Room not found.");
            return;
        }

        if (r.addOccupant(student.getStudentNumber())) {
            student.setHostelRoom(id);
            DataPersistence.saveHostels(hostels);
            DataPersistence.saveStudents(StudentService.getInstance().getStudentsMap());
            box.success("Application successful. Assigned to " + r.getRoomNumber());
        } else {
            box.error("Room " + r.getRoomNumber() + " is full.");
        }
    }

    private void vacateRoomFlow(BoxUI box, Student student) {
        String roomId = student.getHostelRoom();
        if (roomId == null) {
            box.error("You don't have an assigned room.");
            return;
        }

        HostelRoom r = hostels.get(roomId);
        if (r != null) {
            r.removeOccupant(student.getStudentNumber());
        }
        student.setHostelRoom(null);
        DataPersistence.saveHostels(hostels);
        DataPersistence.saveStudents(StudentService.getInstance().getStudentsMap());
        box.success("You have vacated your room.");
    }

    private void viewMyRoom(BoxUI box, Student student) {
        String roomId = student.getHostelRoom();
        if (roomId == null) {
            box.info("You are not currently assigned to any room.");
            return;
        }
        HostelRoom r = hostels.get(roomId);
        if (r != null) {
            box.info("Your Room: " + r.toString());
        } else {
            box.error("Room details not found for ID: " + roomId);
        }
    }

    // --- Common ---

    private void listAllRooms(BoxUI box) {
        CustomArrayList<HostelRoom> all = hostels.values();
        if (all.isEmpty()) {
            box.info("No hostel rooms registered.");
            return;
        }
        String[] lines = new String[all.size()];
        for (int i = 0; i < all.size(); i++) {
            lines[i] = all.get(i).toString();
        }
        box.printSection("Hostel Inventory", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    private void listAvailableRooms(BoxUI box) {
        CustomArrayList<HostelRoom> all = hostels.values();
        CustomArrayList<String> linesList = new CustomArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            HostelRoom r = all.get(i);
            if (r.isAvailable()) {
                linesList.add(r.toString());
            }
        }

        if (linesList.isEmpty()) {
            box.info("No rooms currently available.");
            return;
        }

        String[] lines = new String[linesList.size()];
        for (int i = 0; i < linesList.size(); i++) {
            lines[i] = linesList.get(i);
        }

        box.printSection("Available Rooms", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }
}
