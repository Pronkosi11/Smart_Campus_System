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

    private final CustomQueue<Ticket> pendingTickets;
    private final CustomArrayList<Ticket> resolvedTickets;
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

    public String getAdminStatusMessage() {
        return "Help Desk module is available. Use option 5 to manage tickets.";
    }

    public String getStudentStatusMessage() {
        return "Help Desk module is available. Use option 5 to submit/view your tickets.";
    }

    public void showAdminHelpDeskMenu(BoxUI box) {
        int choice;
        do {
            box.printMenu("HELP DESK - ADMIN", new String[]{
                    "1. View Pending Tickets",
                    "2. Process Next Ticket",
                    "3. View Tickets by Status",
                    "4. Back"
            });
            choice = box.readInt("Choose option: ", 1, 4);
            switch (choice) {
                case 1:
                    displayPendingTickets(box);
                    break;
                case 2:
                    processNextTicket(box);
                    break;
                case 3:
                    displayTicketsByStatus(box);
                    break;
                case 4:
                    break;
                default:
                    box.error("Invalid option.");
            }
        } while (choice != 4);
    }

    public void showStudentHelpDeskMenu(BoxUI box, Student student) {
        int choice;
        do {
            box.printMenu("HELP DESK - STUDENT", new String[]{
                    "1. Submit Ticket",
                    "2. View My Tickets",
                    "3. View Ticket History",
                    "4. Back"
            });
            choice = box.readInt("Choose option: ", 1, 4);
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
        } while (choice != 4);
    }

    public Ticket submitTicket(String studentNumber, String subject, String description) {
        if (studentNumber == null || studentNumber.isBlank() || subject == null || subject.isBlank()
                || description == null || description.isBlank()) {
            return null;
        }
        Ticket t = new Ticket(nextTicketId(), studentNumber.trim(), subject.trim(), description.trim());
        pendingTickets.enqueue(t);
        DataPersistence.saveTickets(pendingTickets, resolvedTickets, ticketCounter);
        return t;
    }

    public Ticket getNextTicket() {
        if (pendingTickets.isEmpty()) {
            return null;
        }
        return pendingTickets.peek();
    }

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

    public CustomArrayList<Ticket> getAllTickets() {
        CustomArrayList<Ticket> all = snapshotPendingTickets();
        for (int i = 0; i < resolvedTickets.size(); i++) {
            all.add(resolvedTickets.get(i));
        }
        return all;
    }

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

    public CustomArrayList<Ticket> getStudentTickets(String studentNumber) {
        CustomArrayList<Ticket> out = new CustomArrayList<>();
        if (studentNumber == null || studentNumber.isBlank()) {
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

    public CustomStack<String> getTicketHistory(String ticketId) {
        Ticket t = findTicketById(ticketId);
        if (t == null) {
            return null;
        }
        return t.getStatusHistory();
    }

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

    public CustomArrayList<Ticket> getResolvedTickets() {
        return resolvedTickets;
    }

    public int getTicketCounter() {
        return ticketCounter;
    }

    private String nextTicketId() {
        ticketCounter++;
        return "TKT-" + ticketCounter;
    }

    private void submitTicketFlow(BoxUI box, Student student) {
        String subject = box.prompt("Subject: ");
        String description = box.prompt("Description: ");
        Ticket t = submitTicket(student.getStudentNumber(), subject, description);
        if (t == null) {
            box.error("Subject and description are required.");
            return;
        }
        box.success("Ticket submitted. Reference: " + t.getId());
    }

    private void displayStudentTickets(BoxUI box, Student student) {
        CustomArrayList<Ticket> tickets = getStudentTickets(student.getStudentNumber());
        if (tickets.isEmpty()) {
            box.info("You have no tickets.");
            return;
        }
        box.printSection("MY HELP DESK TICKETS");
        for (int i = 0; i < tickets.size(); i++) {
            Ticket t = tickets.get(i);
            box.line((i + 1) + ". " + t.getId() + " | " + t.getSubject() + " | " + t.getStatus());
        }
        box.endSection();
    }

    private void displayTicketHistoryFlow(BoxUI box, Student student) {
        String ticketId = box.prompt("Ticket ID: ");
        Ticket t = findTicketById(ticketId);
        if (t == null || !student.getStudentNumber().equalsIgnoreCase(t.getStudentNumber())) {
            box.error("Ticket not found.");
            return;
        }
        CustomArrayList<String> history = snapshotStack(t.getStatusHistory());
        box.printSection("TICKET HISTORY - " + t.getId());
        for (int i = 0; i < history.size(); i++) {
            box.line((i + 1) + ". " + history.get(i));
        }
        box.endSection();
    }

    private void displayPendingTickets(BoxUI box) {
        CustomArrayList<Ticket> pending = snapshotPendingTickets();
        if (pending.isEmpty()) {
            box.info("No pending tickets.");
            return;
        }
        box.printSection("PENDING TICKETS");
        for (int i = 0; i < pending.size(); i++) {
            Ticket t = pending.get(i);
            box.line((i + 1) + ". " + t.getId() + " | " + t.getSubject() + " | " + t.getStudentNumber());
        }
        box.endSection();
    }

    private void processNextTicket(BoxUI box) {
        Ticket next = getNextTicket();
        if (next == null) {
            box.info("No pending tickets to process.");
            return;
        }
        box.printSection("PROCESS NEXT TICKET");
        box.line("Ticket: " + next.getId());
        box.line("Student: " + next.getStudentNumber());
        box.line("Subject: " + next.getSubject());
        box.line("Current Status: " + next.getStatus());
        box.line("Description: " + next.getDescription());
        box.endSection();

        box.printMenu("UPDATE STATUS", new String[]{
                "1. open",
                "2. in-progress",
                "3. closed",
                "4. Cancel"
        });
        int statusOption = box.readInt("Choose option: ", 1, 4);
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

    private void displayTicketsByStatus(BoxUI box) {
        box.printMenu("FILTER TICKETS", new String[]{
                "1. open",
                "2. in-progress",
                "3. closed",
                "4. Back"
        });
        int option = box.readInt("Choose option: ", 1, 4);
        if (option == 4) {
            return;
        }
        String status = option == 1 ? Ticket.STATUS_OPEN
                : option == 2 ? Ticket.STATUS_IN_PROGRESS : Ticket.STATUS_CLOSED;
        CustomArrayList<Ticket> tickets = getTicketsByStatus(status);
        if (tickets.isEmpty()) {
            box.info("No tickets with status '" + status + "'.");
            return;
        }
        box.printSection("TICKETS - " + status.toUpperCase());
        for (int i = 0; i < tickets.size(); i++) {
            Ticket t = tickets.get(i);
            box.line((i + 1) + ". " + t.getId() + " | " + t.getSubject() + " | " + t.getStudentNumber());
        }
        box.endSection();
    }

    private Ticket findTicketById(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
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
}
