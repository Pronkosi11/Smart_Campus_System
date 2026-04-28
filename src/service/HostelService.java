package service;

import datastructures.CustomArrayList;
import datastructures.CustomHashMap;
import datastructures.CustomQueue;
import model.HostelApplication;
import model.HostelRoom;
import model.Student;
import persistence.DataPersistence;
import ui.BoxUI;

/**
 * HostelService - Hostel Module Business Logic
 *
 * This class manages student accommodation. The flow looks like this:
 *
 *   1. The admin maintains a catalogue of {@link HostelRoom} records
 *      (read from {@code data/hostels.json}). Each hostel has a name,
 *      an "allocation group" (e.g. "Females Undergraduates") and a
 *      capacity.
 *
 *   2. Students browse the catalogue and submit a {@link HostelApplication}
 *      for a hostel they want. Applications go into a FIFO QUEUE so the
 *      first student to apply is the first one the admin sees.
 *
 *   3. The admin processes the next application in the queue and either
 *      APPROVES it (the student is then allocated to that hostel) or
 *      REJECTS it.
 *
 * Why these data structures?
 * --------------------------
 *  - {@link CustomHashMap} keyed by hostel name: O(1) lookup of any
 *    hostel record.
 *  - {@link CustomQueue} for pending applications: pure FIFO is exactly
 *    what "first come, first served" needs.
 *  - {@link CustomArrayList} for the processed-applications history.
 */
public class HostelService {

    // ---- Singleton plumbing ----
    private static HostelService instance;

    // Master catalogue of residences keyed by hostel name.
    private CustomHashMap<String, HostelRoom> hostels;
    // Pending applications waiting for an admin decision (FIFO).
    private final CustomQueue<HostelApplication> pendingApplications;
    // History of every application that has been approved or rejected.
    private final CustomArrayList<HostelApplication> processedApplications;

    private HostelService() {
        this.hostels = new CustomHashMap<>();
        this.pendingApplications = new CustomQueue<>();
        this.processedApplications = new CustomArrayList<>();
    }

    public static HostelService getInstance() {
        if (instance == null) {
            instance = new HostelService();
        }
        return instance;
    }

    // ============================================================
    //  PERSISTENCE INTEGRATION
    // ============================================================

    /**
     * Replaces the entire hostel catalogue. Used by the persistence
     * layer when {@code data/hostels.json} is loaded.
     */
    public void setHostels(CustomHashMap<String, HostelRoom> loaded) {
        this.hostels = (loaded != null) ? loaded : new CustomHashMap<>();
    }

    /**
     * Replaces application state from persistence.
     */
    public void restoreApplications(CustomQueue<HostelApplication> pending,
                                    CustomArrayList<HostelApplication> processed) {
        pendingApplications.clear();
        processedApplications.clear();
        if (pending != null) {
            while (!pending.isEmpty()) {
                pendingApplications.enqueue(pending.dequeue());
            }
        }
        if (processed != null) {
            for (int i = 0; i < processed.size(); i++) {
                processedApplications.add(processed.get(i));
            }
        }
    }

    public CustomHashMap<String, HostelRoom> getHostelsMap() { return hostels; }

    public CustomArrayList<HostelApplication> getProcessedApplications() { return processedApplications; }

    /**
     * Returns the pending queue without disturbing it. Used by
     * persistence to write the current state to JSON.
     */
    public CustomQueue<HostelApplication> getPendingApplicationsSnapshotAsQueue() {
        CustomQueue<HostelApplication> copy = new CustomQueue<>();
        CustomQueue<HostelApplication> temp = new CustomQueue<>();
        while (!pendingApplications.isEmpty()) {
            HostelApplication a = pendingApplications.dequeue();
            copy.enqueue(a);
            temp.enqueue(a);
        }
        while (!temp.isEmpty()) {
            pendingApplications.enqueue(temp.dequeue());
        }
        return copy;
    }

    // ============================================================
    //  CORE OPERATIONS
    // ============================================================

    /**
     * Returns every hostel in the catalogue.
     */
    public CustomArrayList<HostelRoom> getAllHostels() {
        return hostels.values();
    }

    /**
     * Returns hostels that still have free space.
     */
    public CustomArrayList<HostelRoom> getAvailableHostels() {
        CustomArrayList<HostelRoom> avail = new CustomArrayList<>();
        CustomArrayList<HostelRoom> all = hostels.values();
        for (int i = 0; i < all.size(); i++) {
            HostelRoom h = all.get(i);
            if (!h.isFull()) {
                avail.add(h);
            }
        }
        return avail;
    }

    public HostelRoom getHostel(String name) { return hostels.get(name); }

    /**
     * Possible outcomes when a student applies for a hostel. The UI
     * uses these to show a friendly message.
     */
    public enum ApplyResult {
        SUCCESS,            // application accepted into the queue
        ALREADY_ALLOCATED,  // student already lives somewhere
        ALREADY_PENDING,    // student already has a pending application
        HOSTEL_NOT_FOUND,   // bad hostel name
        HOSTEL_FULL         // capacity reached
    }

    /**
     * Submits a new hostel application from the given student. The
     * application is queued for the admin to approve or reject later.
     */
    public ApplyResult applyForHostel(String studentNumber, String hostelName) {
        HostelRoom hostel = hostels.get(hostelName);
        if (hostel == null) {
            return ApplyResult.HOSTEL_NOT_FOUND;
        }

        Student student = StudentService.getInstance().getStudent(studentNumber);
        if (student != null && student.getHostelRoom() != null && !student.getHostelRoom().isBlank()) {
            return ApplyResult.ALREADY_ALLOCATED;
        }

        if (hasPendingApplication(studentNumber)) {
            return ApplyResult.ALREADY_PENDING;
        }

        if (hostel.isFull()) {
            return ApplyResult.HOSTEL_FULL;
        }

        pendingApplications.enqueue(new HostelApplication(studentNumber, hostelName));
        DataPersistence.saveHostels(hostels, getPendingApplicationsSnapshotAsQueue(), processedApplications);
        return ApplyResult.SUCCESS;
    }

    /**
     * Looks at the next application without removing it.
     */
    public HostelApplication peekNextApplication() {
        if (pendingApplications.isEmpty()) {
            return null;
        }
        return pendingApplications.peek();
    }

    /**
     * Approves the application currently at the front of the queue.
     * If the hostel is full at this point we reject instead, because
     * we cannot allocate someone to a full hostel.
     */
    public boolean approveNextApplication() {
        if (pendingApplications.isEmpty()) {
            return false;
        }
        HostelApplication app = pendingApplications.dequeue();
        HostelRoom hostel = hostels.get(app.getHostelName());

        if (hostel == null || hostel.isFull()) {
            // Cannot allocate — auto-reject so the queue keeps moving.
            app.setStatus(HostelApplication.STATUS_REJECTED);
            processedApplications.add(app);
            DataPersistence.saveHostels(hostels, getPendingApplicationsSnapshotAsQueue(), processedApplications);
            return false;
        }

        // Allocate the student to the hostel.
        hostel.allocateStudent(app.getStudentNumber());
        Student student = StudentService.getInstance().getStudent(app.getStudentNumber());
        if (student != null) {
            student.setHostelRoom(hostel.getName());
            StudentService.getInstance().updateStudent(student);
        }

        app.setStatus(HostelApplication.STATUS_APPROVED);
        processedApplications.add(app);
        DataPersistence.saveHostels(hostels, getPendingApplicationsSnapshotAsQueue(), processedApplications);
        return true;
    }

    /**
     * Rejects the application at the front of the queue.
     */
    public boolean rejectNextApplication() {
        if (pendingApplications.isEmpty()) {
            return false;
        }
        HostelApplication app = pendingApplications.dequeue();
        app.setStatus(HostelApplication.STATUS_REJECTED);
        processedApplications.add(app);
        DataPersistence.saveHostels(hostels, getPendingApplicationsSnapshotAsQueue(), processedApplications);
        return true;
    }

    /**
     * Removes a student from their current hostel allocation. Used by
     * the admin to free up a bed.
     */
    public boolean evictStudent(String studentNumber) {
        Student student = StudentService.getInstance().getStudent(studentNumber);
        if (student == null) {
            return false;
        }
        String currentHostel = student.getHostelRoom();
        if (currentHostel == null || currentHostel.isBlank()) {
            return false;
        }
        HostelRoom hostel = hostels.get(currentHostel);
        if (hostel != null) {
            hostel.removeStudent(studentNumber);
        }
        student.setHostelRoom(null);
        StudentService.getInstance().updateStudent(student);
        DataPersistence.saveHostels(hostels, getPendingApplicationsSnapshotAsQueue(), processedApplications);
        return true;
    }

    // ============================================================
    //  ADMIN MENU
    // ============================================================

    public void showAdminHostelMenu(BoxUI box) {
        int choice;
        do {
            box.printMenu("MANAGE HOSTELS", new String[]{
                    "1. List All Hostels",
                    "2. View Pending Applications",
                    "3. Process Next Application",
                    "4. View Application History",
                    "5. Evict Student",
                    "6. Back"
            });
            choice = box.readInt("Choose option: ", 1, 6);
            switch (choice) {
                case 1: listHostels(box); break;
                case 2: listPendingApplications(box); break;
                case 3: processNextApplicationFlow(box); break;
                case 4: listProcessedApplications(box); break;
                case 5: evictStudentFlow(box); break;
                case 6: break;
                default: box.error("Invalid option.");
            }
        } while (choice != 6);
    }

    // ============================================================
    //  STUDENT MENU
    // ============================================================

    public void showStudentHostelMenu(BoxUI box, Student student) {
        int choice;
        do {
            box.printMenu("HOSTEL APPLICATION", new String[]{
                    "1. View Available Hostels",
                    "2. Apply for a Hostel",
                    "3. Check My Application Status",
                    "4. Back"
            });
            choice = box.readInt("Choose option: ", 1, 4);
            switch (choice) {
                case 1: listAvailableHostels(box); break;
                case 2: applyFlow(box, student); break;
                case 3: showMyStatus(box, student); break;
                case 4: break;
                default: box.error("Invalid option.");
            }
        } while (choice != 4);
    }

    // ============================================================
    //  UI FLOW HELPERS
    // ============================================================

    private void listHostels(BoxUI box) {
        CustomArrayList<HostelRoom> all = getAllHostels();
        if (all.isEmpty()) { box.info("No hostels configured."); return; }
        box.printSection("ALL HOSTELS");
        for (int i = 0; i < all.size(); i++) {
            box.line((i + 1) + ". " + all.get(i));
        }
        box.endSection();
    }

    private void listAvailableHostels(BoxUI box) {
        CustomArrayList<HostelRoom> avail = getAvailableHostels();
        if (avail.isEmpty()) { box.info("No hostels currently have free space."); return; }
        box.printSection("AVAILABLE HOSTELS");
        for (int i = 0; i < avail.size(); i++) {
            box.line((i + 1) + ". " + avail.get(i));
        }
        box.endSection();
    }

    private void listPendingApplications(BoxUI box) {
        CustomQueue<HostelApplication> snapshot = getPendingApplicationsSnapshotAsQueue();
        if (snapshot.isEmpty()) { box.info("No pending applications."); return; }
        box.printSection("PENDING APPLICATIONS (FIFO)");
        int idx = 1;
        while (!snapshot.isEmpty()) {
            box.line(idx + ". " + snapshot.dequeue());
            idx++;
        }
        box.endSection();
    }

    private void processNextApplicationFlow(BoxUI box) {
        HostelApplication next = peekNextApplication();
        if (next == null) { box.info("No pending applications to process."); return; }

        box.printSection("NEXT APPLICATION");
        box.line("Student : " + next.getStudentNumber());
        box.line("Hostel  : " + next.getHostelName());
        box.line("Applied : " + next.getAppliedAt());
        box.endSection();

        box.printMenu("DECISION", new String[]{
                "1. Approve (allocate student to hostel)",
                "2. Reject",
                "3. Cancel (leave it in the queue)"
        });
        int decision = box.readInt("Choose: ", 1, 3);
        if (decision == 1) {
            if (approveNextApplication()) {
                box.success("Approved and student allocated.");
            } else {
                box.error("Could not approve — hostel may be full. Application has been rejected automatically.");
            }
        } else if (decision == 2) {
            if (rejectNextApplication()) {
                box.success("Application rejected.");
            } else {
                box.error("Could not reject application.");
            }
        }
        // decision == 3 -> do nothing.
    }

    private void listProcessedApplications(BoxUI box) {
        if (processedApplications.isEmpty()) { box.info("No processed applications yet."); return; }
        box.printSection("APPLICATION HISTORY");
        for (int i = 0; i < processedApplications.size(); i++) {
            box.line((i + 1) + ". " + processedApplications.get(i));
        }
        box.endSection();
    }

    private void evictStudentFlow(BoxUI box) {
        String num = box.prompt("Student number to evict: ");
        if (evictStudent(num)) {
            box.success("Student evicted from their hostel.");
        } else {
            box.error("Could not evict — student not found or not allocated.");
        }
    }

    private void applyFlow(BoxUI box, Student student) {
        if (student.getHostelRoom() != null && !student.getHostelRoom().isBlank()) {
            box.error("You already live in: " + student.getHostelRoom());
            return;
        }
        if (hasPendingApplication(student.getStudentNumber())) {
            box.error("You already have a pending application.");
            return;
        }
        String hostelName = box.prompt("Hostel name (exactly as shown in the list): ");
        ApplyResult result = applyForHostel(student.getStudentNumber(), hostelName);
        switch (result) {
            case SUCCESS:           box.success("Application submitted. The admin will review it."); break;
            case ALREADY_ALLOCATED: box.error("You already have a hostel allocation."); break;
            case ALREADY_PENDING:   box.error("You already have a pending application."); break;
            case HOSTEL_NOT_FOUND:  box.error("No hostel found with that name."); break;
            case HOSTEL_FULL:       box.error("That hostel is full."); break;
        }
    }

    private void showMyStatus(BoxUI box, Student student) {
        box.printSection("MY HOSTEL STATUS");
        if (student.getHostelRoom() != null && !student.getHostelRoom().isBlank()) {
            box.line("Allocated Hostel: " + student.getHostelRoom());
        } else {
            box.line("Allocated Hostel: (none)");
        }

        // Look for a pending application.
        HostelApplication pending = findPendingApplication(student.getStudentNumber());
        if (pending != null) {
            box.line("Pending Application: " + pending.getHostelName() + " (applied " + pending.getAppliedAt() + ")");
        } else {
            box.line("Pending Application: (none)");
        }

        // Show last decision (most recent processed application).
        HostelApplication latest = findLatestProcessed(student.getStudentNumber());
        if (latest != null) {
            box.line("Last Decision: " + latest.getStatus() + " for " + latest.getHostelName());
        }
        box.endSection();
    }

    // ============================================================
    //  PRIVATE UTILITIES
    // ============================================================

    /**
     * Quick check: does this student already have a pending application?
     */
    private boolean hasPendingApplication(String studentNumber) {
        return findPendingApplication(studentNumber) != null;
    }

    /**
     * Returns the student's pending application, or null if none.
     * Snapshots the queue so its state is preserved.
     */
    private HostelApplication findPendingApplication(String studentNumber) {
        CustomQueue<HostelApplication> snapshot = getPendingApplicationsSnapshotAsQueue();
        while (!snapshot.isEmpty()) {
            HostelApplication a = snapshot.dequeue();
            if (a.getStudentNumber().equalsIgnoreCase(studentNumber)) {
                return a;
            }
        }
        return null;
    }

    /**
     * Returns the most recent processed application for a student.
     */
    private HostelApplication findLatestProcessed(String studentNumber) {
        HostelApplication latest = null;
        for (int i = 0; i < processedApplications.size(); i++) {
            HostelApplication a = processedApplications.get(i);
            if (a.getStudentNumber().equalsIgnoreCase(studentNumber)) {
                latest = a; // keep overwriting — last one wins
            }
        }
        return latest;
    }
}
