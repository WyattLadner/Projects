package edu.southalabama.passwordmanager.server;

public class passwordEntry {

    private String service;
    private String username;
    private String password;

    public passwordEntry(String service, String username, String password) {
        this.service = service;
        this.username = username;
        this.password = password;
    }

    public String getService() { return service; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "Service: " + service + ", Username: " + username + ", Pass: " + password;
    }
}
