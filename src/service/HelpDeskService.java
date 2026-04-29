package service;

import datastructures.CustomArrayList;
import datastructures.CustomQueue;
import datastructures.CustomStack;
import model.Student;
import model.Ticket;
import persistence.DataPersistence;
import ui.BoxUI;

public class HelpDeskService {
    private static HelpDeskService instance;

    // Queue keeps open/in-progress tickets in FIFO order.
    private final CustomQueue<Ticket> pendingTickets;
    // Closed tickets are archived here for history and reporting.
    private final CustomArrayList<Ticket> resolvedTickets;
    // Counter used to generate readable IDs (TKT-1001, TKT-1002, ...).
    private int ticketCounter;

    private HelpDeskService() {
        this.pendingTickets = new CustomQueue<>();
        this.resolvedTickets = new CustomArrayList<>();
        this.ticketCounter = 1000;
    }

    public static HelpDeskService getInstance() {
        if (instance == null) {
            instance = new HelpDeskService();
        }
        return instance;
    }

    /**
     * Returns a short message shown from the admin dashboard.
     */
    public String getAdminStatusMessage() {
        return "Help Desk module is available. Use option 5 to manage tickets.";
    }

    /**
     * Returns a short message shown from the student portal.
     */
    public String getStudentStatusMessage() {
        return "Help Desk module is available. Use option 5 to submit/view your tickets.";
    }

    /**
     * Admin entry-point for the Help Desk module.
     * Renders the admin ticket menu and dispatches actions.
     */
    public void showAdminHelpDeskMenu(BoxUI box) {
        int choice;
        do {
            box.info("Admin Help Desk: review student issues, update their status, and close resolved tickets.");
            box.printMenu("HELP DESK - ADMIN", new String[]{
                    "1. View All Tickets",
                    "2. View Pending Tickets",
                    "3. Process Next Ticket",
                    "4. Update Ticket Status",
                    "5. Search Ticket by ID",
                    "6. Back to Admin Dashboard"
            });
            choice = box.readInt("Choose an admin option: ", 1, 6);
            switch (choice) {
                case 1:
                    displayAllTickets(box);
                    break;
                case 2:
                    displayPendingTickets(box);
                    break;
                case 3:
                    processNextTicket(box);
                    break;
                case 4:
                    updateTicketStatus(box);
                    break;
                case 5:
                    searchTicketById(box);
                    break;
                case 6:
                    break;
                default:
                    box.error("Invalid option.");
            }
        } while (choice != 6);
    }

    /**
     * Student entry-point for the Help Desk module.
     * Renders the student ticket menu and dispatches actions for the logged-in student.
     */
    public void showStudentHelpDeskMenu(BoxUI box, Student student) {
        int choice;
        do {
            box.info("Student Help Desk: submit new issues and track updates to your existing tickets.");
            box.printMenu("HELP DESK - STUDENT", new String[]{
                    "1. Submit Ticket (create a new support request)",
                    "2. View My Tickets (current status overview)",
                    "3. View Ticket History (status timeline)",
                    "4. Back to Student Portal"
            });
            choice = box.readInt("Choose a student option: ", 1, 4);
            switch (choice) {
                case 1:
                    submitTicketFlow(box, student);
                    break;
                case 2:
                    displayStudentTickets(box, student);
                    break;
                case 3:
                    displayTicketHistoryFlow(box, student);
                    break;
                case 4:
                    break;
                default:
                    box.error("Invalid option.");
            }
        } while (choice != 6);
    }

    /**
     * Creates and queues a new ticket.
     * Validation is intentionally simple for console UX: all fields must be non-empty.
     */
    public Ticket submitTicket(String studentNumber, String subject, String description) {
        if (isBlank(studentNumber) || isBlank(subject) || isBlank(description)) {
            return null;
        }
        Ticket t = new Ticket(nextTicketId(), studentNumber.trim(), subject.trim(), description.trim());
        pendingTickets.enqueue(t); // New tickets always join queue tail.
        DataPersistence.saveTickets(pendingTickets, resolvedTickets, ticketCounter);
        return t;
    }

    /**
     * Returns the next ticket to process without removing it.
     * This keeps FIFO order visible to the admin before status update.
     */
    public Ticket getNextTicket() {
        if (pendingTickets.isEmpty()) {
            return null;
        }
        return pendingTickets.peek();
    }

    /**
     * Updates a ticket status and archives it when moved to "closed".
     * Closed tickets are removed from the pending queue and moved to resolved storage.
     */
    public boolean updateTicketStatus(String ticketId, String status) {
        Ticket t = findTicketById(ticketId);
        if (t == null || !Ticket.isValidStatus(status)) {
            return false;
        }
        String oldStatus = t.getStatus();
        t.updateStatus(status);

        if (!oldStatus.equals(Ticket.STATUS_CLOSED) && Ticket.STATUS_CLOSED.equals(t.getStatus())) {
            removeTicketFromPending(ticketId);
            resolvedTickets.add(t);
        }
        DataPersistence.saveTickets(pendingTickets, resolvedTickets, ticketCounter);
        return true;
    }

    /**
     * Returns a merged view of pending + resolved tickets.
     * Pending tickets are copied from queue snapshot to avoid mutating queue state.
     */
    public CustomArrayList<Ticket> getAllTickets() {
        CustomArrayList<Ticket> all = snapshotPendingTickets();
        for (int i = 0; i < resolvedTickets.size(); i++) {
            all.add(resolvedTickets.get(i));
        }
        return all;
    }

    /**
     * Filters all tickets by one of the allowed statuses.
     */
    public CustomArrayList<Ticket> getTicketsByStatus(String status) {
        CustomArrayList<Ticket> filtered = new CustomArrayList<>();
        if (!Ticket.isValidStatus(status)) {
            return filtered;
        }
        String target = status.trim().toLowerCase();
        CustomArrayList<Ticket> all = getAllTickets();
        for (int i = 0; i < all.size(); i++) {
            Ticket t = all.get(i);
            if (target.equals(t.getStatus())) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    /**
     * Returns all tickets created by a single student.
     */
    public CustomArrayList<Ticket> getStudentTickets(String studentNumber) {
        CustomArrayList<Ticket> out = new CustomArrayList<>();
        if (isBlank(studentNumber)) {
            return out;
        }
        String target = studentNumber.trim();
        CustomArrayList<Ticket> all = getAllTickets();
        for (int i = 0; i < all.size(); i++) {
            Ticket t = all.get(i);
            if (target.equalsIgnoreCase(t.getStudentNumber())) {
                out.add(t);
            }
        }
        return out;
    }

    /**
     * Returns a ticket's status history stack.
     * Returns null when ticket does not exist.
     */
    public CustomStack<String> getTicketHistory(String ticketId) {
        Ticket t = findTicketById(ticketId);
        if (t == null) {
            return null;
        }
        return t.getStatusHistory();
    }

    /**
     * Rehydrates service state from persistence during startup.
     */
    public void restoreTicketData(CustomQueue<Ticket> pending, CustomArrayList<Ticket> resolved, int counter) {
        pendingTickets.clear();
        resolvedTickets.clear();
        if (pending != null) {
            while (!pending.isEmpty()) {
                pendingTickets.enqueue(pending.dequeue());
            }
        }
        if (resolved != null) {
            for (int i = 0; i < resolved.size(); i++) {
                resolvedTickets.add(resolved.get(i));
            }
        }
        ticketCounter = Math.max(counter, 1000);
    }

    /**
     * Returns a copy of pending tickets in queue form.
     * Internally rotates through a temp queue to preserve original queue order.
     */
    public CustomQueue<Ticket> getPendingTicketsSnapshotAsQueue() {
        CustomQueue<Ticket> copy = new CustomQueue<>();
        CustomQueue<Ticket> temp = new CustomQueue<>();
        while (!pendingTickets.isEmpty()) {
            Ticket t = pendingTickets.dequeue();
            copy.enqueue(t);
            temp.enqueue(t);
        }
        while (!temp.isEmpty()) {
            pendingTickets.enqueue(temp.dequeue());
        }
        return copy;
    }

    /**
     * Exposes resolved ticket archive.
     */
    public CustomArrayList<Ticket> getResolvedTickets() {
        return resolvedTickets;
    }

    /**
     * Returns current ID counter for persistence.
     */
    public int getTicketCounter() {
        return ticketCounter;
    }

    /**
     * Generates next ticket reference number.
     */
    private String nextTicketId() {
        ticketCounter++;
        return "TKT-" + ticketCounter;
    }

    /**
     * Student flow to submit a ticket from UI prompts.
     */
    private void submitTicketFlow(BoxUI box, Student student) {
        String subject = box.prompt("Subject (short title): ");
        String description = box.prompt("Description (full details of the issue): ");
        Ticket t = submitTicket(student.getStudentNumber(), subject, description);
        if (t == null) {
            box.error("Subject and description are required.");
            return;
        }
        box.success("Ticket submitted. Reference: " + t.getId());
    }

    /**
     * Prints all tickets owned by the current student.
     */
    private void displayStudentTickets(BoxUI box, Student student) {
        CustomArrayList<Ticket> tickets = getStudentTickets(student.getStudentNumber());
        if (tickets.isEmpty()) {
            box.info("You have no tickets.");
            return;
        }
        String[] lines = new String[tickets.size()];
        for (int i = 0; i < tickets.size(); i++) {
            Ticket t = tickets.get(i);
            lines[i] = (i + 1) + ". " + t.getId() + " | " + t.getSubject() + " | " + t.getStatus();
        }
        box.printSection("MY HELP DESK TICKETS", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    /**
     * Shows status history for one student-owned ticket.
     * History is copied from stack so original order/state remains unchanged.
     */
    private void displayTicketHistoryFlow(BoxUI box, Student student) {
        String ticketId = box.prompt("Ticket ID (example: TKT-1001): ");
        Ticket t = findTicketById(ticketId);
        if (t == null || !student.getStudentNumber().equalsIgnoreCase(t.getStudentNumber())) {
            box.error("Ticket not found.");
            return;
        }
        CustomArrayList<String> history = snapshotStack(t.getStatusHistory());
        String[] lines = new String[history.size()];
        for (int i = 0; i < history.size(); i++) {
            lines[i] = (i + 1) + ". " + history.get(i);
        }
        box.printSection("TICKET HISTORY - " + t.getId(), lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    /**
     * Prints current pending queue in processing order.
     */
    private void displayPendingTickets(BoxUI box) {
        CustomArrayList<Ticket> pending = snapshotPendingTickets();
        if (pending.isEmpty()) {
            box.info("No pending tickets.");
            return;
        }
        String[] lines = new String[pending.size()];
        for (int i = 0; i < pending.size(); i++) {
            Ticket t = pending.get(i);
            lines[i] = (i + 1) + ". " + t.getId() + " | " + t.getSubject() + " | " + t.getStudentNumber();
        }
        box.printSection("PENDING TICKETS", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    /**
     * Admin flow that shows the next ticket and allows status update.
     */
    private void processNextTicket(BoxUI box) {
        Ticket next = getNextTicket();
        if (next == null) {
            box.info("No pending tickets to process.");
            return;
        }
        box.printSection("PROCESS NEXT TICKET", 
                "Ticket: " + next.getId(), 
                "Student: " + next.getStudentNumber(),
                "Subject: " + next.getSubject(),
                "Current Status: " + next.getStatus(),
                "Description: " + next.getDescription());
        box.line("Ticket: " + next.getId());
        box.line("Student: " + next.getStudentNumber());
        box.line("Subject: " + next.getSubject());
        box.line("Current Status: " + next.getStatus());
        box.line("Description: " + next.getDescription());
        box.endSection();

        box.printMenu("UPDATE STATUS", new String[]{
                "1. open (ticket received and waiting)",
                "2. in-progress (currently being handled)",
                "3. closed (issue fully resolved)",
                "4. Cancel"
        });
        int statusOption = box.readInt("Choose the new status: ", 1, 4);
        if (statusOption == 4) {
            return;
        }

        String nextStatus = statusOption == 1 ? Ticket.STATUS_OPEN
                : statusOption == 2 ? Ticket.STATUS_IN_PROGRESS : Ticket.STATUS_CLOSED;
        if (updateTicketStatus(next.getId(), nextStatus)) {
            box.success("Ticket " + next.getId() + " updated to '" + nextStatus + "'.");
        } else {
            box.error("Could not update ticket.");
        }
    }

    /**
     * Displays all tickets (both pending and resolved).
     */
    private void displayAllTickets(BoxUI box) {
        CustomArrayList<Ticket> all = getAllTickets();
        if (all.isEmpty()) {
            box.info("No tickets found.");
            return;
        }
        String[] lines = new String[all.size()];
        for (int i = 0; i < all.size(); i++) {
            Ticket t = all.get(i);
            lines[i] = (i + 1) + ". " + t.getId() + " | " + t.getSubject() + " | " + t.getStatus() + " | " + t.getStudentNumber();
        }
        box.printSection("ALL TICKETS", lines);
        for (String line : lines) {
            box.line(line);
        }
        box.endSection();
    }

    /**
     * Updates ticket status with user-friendly interface.
     */
    private void updateTicketStatus(BoxUI box) {
        String ticketId = box.prompt("Enter Ticket ID: ");
        if (isBlank(ticketId)) {
            box.error("Ticket ID is required.");
            return;
        }
        
        Ticket t = findTicketById(ticketId);
        if (t == null) {
            box.error("Ticket not found.");
            return;
        }
        
        box.printSection("UPDATE TICKET STATUS", 
                "Current ID: " + t.getId(),
                "Current Status: " + t.getStatus(),
                "Student: " + t.getStudentNumber());
        box.line("Current ID: " + t.getId());
        box.line("Current Status: " + t.getStatus());
        box.line("Student: " + t.getStudentNumber());
        box.endSection();
        
        box.printMenu("NEW STATUS", new String[]{
                "1. open (ticket received and waiting)",
                "2. in-progress (currently being handled)",
                "3. closed (issue fully resolved)",
                "4. Cancel"
        });
        int statusOption = box.readInt("Choose new status: ", 1, 4);
        if (statusOption == 4) {
            return;
        }
        
        String[] statuses = {"open", "in-progress", "closed"};
        if (statusOption >= 1 && statusOption <= 3) {
            String newStatus = statuses[statusOption - 1];
            if (updateTicketStatus(ticketId, newStatus)) {
                box.success("Ticket status updated to: " + newStatus);
            } else {
                box.error("Failed to update ticket status.");
            }
        }
    }

    /**
     * Searches for a specific ticket by ID.
     */
    private void searchTicketById(BoxUI box) {
        String ticketId = box.prompt("Enter Ticket ID: ");
        if (isBlank(ticketId)) {
            box.error("Ticket ID is required.");
            return;
        }
        
        Ticket t = findTicketById(ticketId);
        if (t == null) {
            box.error("Ticket not found.");
            return;
        }
        
        box.printSection("TICKET DETAILS", 
                "ID: " + t.getId(),
                "Student: " + t.getStudentNumber(),
                "Subject: " + t.getSubject(),
                "Status: " + t.getStatus(),
                "Created: " + (t.getCreatedAt() != null ? t.getCreatedAt().toString() : "N/A"),
                "Description: " + t.getDescription());
        box.line("ID: " + t.getId());
        box.line("Student: " + t.getStudentNumber());
        box.line("Subject: " + t.getSubject());
        box.line("Status: " + t.getStatus());
        box.line("Created: " + (t.getCreatedAt() != null ? t.getCreatedAt().toString() : "N/A"));
        box.line("Description: " + t.getDescription());
        box.endSection();
    }

    /**
     * Admin flow for filtering tickets by status and printing results.
     */
    private void displayTicketsByStatus(BoxUI box) {
        box.printMenu("FILTER TICKETS", new String[]{
                "1. open (new/unhandled)",
                "2. in-progress (actively worked on)",
                "3. closed (resolved)",
                "4. Back"
        });

        int option = box.readInt("Choose a status filter: ", 1, 4);
        if (option == 4) {
            return;
        }

        String selectedStatus = mapStatusOption(option);
        CustomArrayList<Ticket> tickets = getTicketsByStatus(selectedStatus);
        if (tickets.isEmpty()) {
            box.info("No tickets with status '" + selectedStatus + "'.");
            return;
        }

        box.printSection("TICKETS - " + selectedStatus.toUpperCase(), tickets.size() > 0 ? new String[]{"Longer subject title placeholder for sizing"} : new String[0]);
        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);
            String row = (i + 1) + ". " + ticket.getId()
                    + " | " + ticket.getSubject()
                    + " | " + ticket.getStudentNumber();
            box.line(row);
        }
        box.endSection();
    }

    /**
     * Maps menu option number to a valid ticket status constant.
     */
    private String mapStatusOption(int option) {
        if (option == 1) return Ticket.STATUS_OPEN;
        if (option == 2) return Ticket.STATUS_IN_PROGRESS;
        return Ticket.STATUS_CLOSED;
    }

    /**
     * Finds ticket by ID across pending queue and resolved archive.
     */
    private Ticket findTicketById(String ticketId) {
        if (isBlank(ticketId)) {
            return null;
        }
        String target = ticketId.trim();
        CustomArrayList<Ticket> pending = snapshotPendingTickets();
        for (int i = 0; i < pending.size(); i++) {
            Ticket t = pending.get(i);
            if (target.equalsIgnoreCase(t.getId())) {
                return t;
            }
        }
        for (int i = 0; i < resolvedTickets.size(); i++) {
            Ticket t = resolvedTickets.get(i);
            if (target.equalsIgnoreCase(t.getId())) {
                return t;
            }
        }
        return null;
    }

    /**
     * Removes a ticket from pending queue while preserving the order of other tickets.
     */
    private void removeTicketFromPending(String ticketId) {
        CustomQueue<Ticket> temp = new CustomQueue<>();
        while (!pendingTickets.isEmpty()) {
            Ticket t = pendingTickets.dequeue();
            if (!ticketId.equalsIgnoreCase(t.getId())) {
                temp.enqueue(t);
            }
        }
        while (!temp.isEmpty()) {
            pendingTickets.enqueue(temp.dequeue());
        }
    }

    /**
     * Creates a list snapshot of pending queue contents.
     * Queue is restored to original state after snapshotting.
     */
    private CustomArrayList<Ticket> snapshotPendingTickets() {
        CustomArrayList<Ticket> copy = new CustomArrayList<>();
        CustomQueue<Ticket> temp = new CustomQueue<>();
        while (!pendingTickets.isEmpty()) {
            Ticket t = pendingTickets.dequeue();
            copy.add(t);
            temp.enqueue(t);
        }
        while (!temp.isEmpty()) {
            pendingTickets.enqueue(temp.dequeue());
        }
        return copy;
    }

    /**
     * Copies stack entries into a list in top-to-bottom order.
     * Original stack is restored so caller does not lose history data.
     */
    private CustomArrayList<String> snapshotStack(CustomStack<String> stack) {
        CustomArrayList<String> out = new CustomArrayList<>();
        CustomStack<String> temp = new CustomStack<>();
        while (!stack.isEmpty()) {
            String item = stack.pop();
            temp.push(item);
            out.add(item);
        }
        while (!temp.isEmpty()) {
            stack.push(temp.pop());
        }
        return out;
    }

    /**
     * Utility helper for text field validation.
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
