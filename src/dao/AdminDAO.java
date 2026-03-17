package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bus_ticket.DBconnection;

/** Handles admin authentication. */
public class AdminDAO {

    public boolean loginAdmin(String username, String password) {
        if (username == null || username.trim().isEmpty()) return false;
        if (password == null || password.trim().isEmpty())  return false;

        String sql = "SELECT 1 FROM admin WHERE username=? AND password=?";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            System.out.println("  ERROR: Could not verify admin credentials.");
        }
        return false;
    }
}