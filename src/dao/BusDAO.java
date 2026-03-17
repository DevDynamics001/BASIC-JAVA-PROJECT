package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bus_ticket.DBconnection;

/** Handles CRUD operations for buses. */
public class BusDAO {

    public void addBus(String name, String type, int totalSeats) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("  Bus name cannot be empty!"); return;
        }
        if (type == null || type.trim().isEmpty()) {
            System.out.println("  Bus type cannot be empty!"); return;
        }
        if (totalSeats <= 0) {
            System.out.println("  Total seats must be greater than 0!"); return;
        }

        String sql = "INSERT INTO buses (bus_name, bus_type, total_seats) VALUES (?, ?, ?)";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, type.trim());
            ps.setInt(3, totalSeats);
            ps.executeUpdate();
            System.out.println("  Bus added successfully.");
        } catch (Exception e) {
            System.out.println("  ERROR: Could not add bus.");
        }
    }

    public void deleteBus(int busId) {
        String checkSchedule = "SELECT COUNT(*) AS cnt FROM bus_schedule WHERE bus_id=?";
        String deleteSeats   = "DELETE FROM seats WHERE bus_id=?";
        String deleteBus     = "DELETE FROM buses WHERE bus_id=?";

        try (Connection con = DBconnection.getConnection()) {
            PreparedStatement psCheck = con.prepareStatement(checkSchedule);
            psCheck.setInt(1, busId);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next() && rs.getInt("cnt") > 0) {
                System.out.println("  Cannot delete! Bus " + busId + " has active schedules."); return;
            }
            PreparedStatement psDel1 = con.prepareStatement(deleteSeats);
            psDel1.setInt(1, busId);
            psDel1.executeUpdate();

            PreparedStatement psDel2 = con.prepareStatement(deleteBus);
            psDel2.setInt(1, busId);
            int rows = psDel2.executeUpdate();
            System.out.println(rows > 0
                    ? "  Bus " + busId + " deleted successfully."
                    : "  Bus ID " + busId + " not found.");
        } catch (Exception e) {
            System.out.println("  ERROR: Could not delete bus.");
        }
    }

    public void viewBuses() {
        String sql = "SELECT bus_id, bus_name, bus_type, total_seats FROM buses ORDER BY bus_id";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n  ================ BUS DETAILS ================");
            System.out.printf("  %-8s %-22s %-14s %-8s%n", "BusID", "Bus Name", "Type", "Seats");
            System.out.println("  ----------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-8d %-22s %-14s %-8d%n",
                        rs.getInt("bus_id"),
                        rs.getString("bus_name"),
                        rs.getString("bus_type"),
                        rs.getInt("total_seats"));
            }
            if (!found) System.out.println("  No buses found.");

        } catch (Exception e) {
            System.out.println("  ERROR: Could not fetch buses.");
        }
    }
}