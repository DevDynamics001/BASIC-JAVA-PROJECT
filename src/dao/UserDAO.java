package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bus_ticket.DBconnection;
import model.User;

/** Handles user registration, login, and profile validation. */
public class UserDAO {

    // ── VALIDATORS ────────────────────────────────────────────────

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[1-9][0-9]{9}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // ── REGISTER ─────────────────────────────────────────────────

    public boolean registerUser(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            System.out.println("  Name cannot be empty!"); return false;
        }
        if (!isValidEmail(user.getEmail())) {
            System.out.println("  Invalid email! Format: name@example.com"); return false;
        }
        if (!isValidPassword(user.getPassword())) {
            System.out.println("  Password must be at least 6 characters!"); return false;
        }
        if (!isValidPhone(user.getPhone())) {
            System.out.println("  Invalid phone! Must be 10 digits and cannot start with 0."); return false;
        }

        String sql = "INSERT INTO users (name, email, password, phone) VALUES (?, ?, ?, ?)";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getName().trim());
            ps.setString(2, user.getEmail().trim().toLowerCase()); // always store normalised
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getPhone().trim());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("ORA-00001") || msg.contains("unique constraint")))
                System.out.println("  Email already registered! Please use a different email.");
            else
                System.out.println("  Registration failed. Please try again.");
        }
        return false;
    }

    // ── LOGIN ─────────────────────────────────────────────────────

    public int loginUser(String email, String password) {
        if (!isValidEmail(email)) { System.out.println("  Invalid email format!"); return -1; }
        if (password == null || password.isEmpty()) { System.out.println("  Password cannot be empty!"); return -1; }

        String sql = "SELECT user_id FROM users WHERE email=? AND password=?";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        } catch (Exception e) {
            System.out.println("  ERROR: Login query failed.");
        }
        return -1;
    }
}