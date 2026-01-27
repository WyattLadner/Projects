package edu.southalabama.passwordmanager.server;

import java.sql.*;
import java.security.MessageDigest;

public class authController {

    private static final String AUTH_DB = "auth.db";
    private Connection conn;

    public authController() {
        initDatabase();
    }

    private void initDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + AUTH_DB);

            String createTable = "CREATE TABLE IF NOT EXISTS auth ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT UNIQUE NOT NULL,"
                    + "password_hash TEXT NOT NULL"
                    + ");";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createTable);
            }

            if (!userExists("admin")) {
                createUser("admin", hash("password"));
                //System.out.println("Created default user: admin/password");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM auth WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private void createUser(String username, String passwordHash) {
        String sql = "INSERT INTO auth (username, password_hash) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean authenticate(String username, String passwordHash) {
        String sql = "SELECT password_hash FROM auth WHERE username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getString("password_hash").equals(passwordHash);

        } catch (SQLException e) {
            return false;
        }
    }

    public boolean changePassword(String username, String oldPasswordHash, String newPasswordHash) {
        if (!authenticate(username, oldPasswordHash)) return false;

        String sql = "UPDATE auth SET password_hash = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPasswordHash);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void close() {
        try { if (conn != null) conn.close(); }
        catch (SQLException ignored) {}
    }
}
