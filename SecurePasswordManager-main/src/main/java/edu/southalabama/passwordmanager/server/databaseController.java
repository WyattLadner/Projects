package edu.southalabama.passwordmanager.server;

import java.sql.*;

public class databaseController {

    private static final String DB_URL = "jdbc:sqlite:securePasswords.db";
    private static final String TABLE_NAME = "Passwords";

    public Connection connect() {
        try {
            //System.out.println("Attempting to connect to SQL Server...");
            Connection conn = DriverManager.getConnection(DB_URL);
            //System.out.println("Database connection successful!");
            return conn;
        } catch (SQLException e) {
           // System.err.println("SQL Connection failed.");
            e.printStackTrace();
            return null;
        }
    }

    public void createTable(Connection conn) {
        if (conn == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "Service TEXT NOT NULL,"
                + "Username TEXT NOT NULL,"
                + "Password TEXT NOT NULL"
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            //System.out.println("Table ready: " + TABLE_NAME);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertData(Connection conn, String service, String username, String password) {
        if (conn == null) return;
        try (PreparedStatement ps =
            conn.prepareStatement("INSERT INTO " + TABLE_NAME + " (Service, Username, Password) VALUES (?,?,?)")
        ) {
            ps.setString(1, service);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String pullPasswords(Connection conn) {
        if (conn == null) return null;

        try (
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM " + TABLE_NAME)
        ) {

            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getString("Service")).append(" ")
                  .append(rs.getString("Username")).append(" ")
                  .append(rs.getString("Password")).append("\n");
            }
            return sb.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteEntry(Connection conn, String service, String username) {
        String sql = "DELETE FROM " + TABLE_NAME +
                     " WHERE LOWER(Service)=LOWER(?) AND LOWER(Username)=LOWER(?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, service);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            //System.err.println("Delete failed: " + e.getMessage());
            return false;
        }
    }
}
