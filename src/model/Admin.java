package model;

public class Admin extends User {

    public Admin(String id, String name, String password) {
        super(id, name, password, "ADMIN");
    }

    @Override
    public String toString() {
        return String.format("Admin - %s", super.toString());
    }
}