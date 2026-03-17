package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bus_ticket.DBconnection;

/** Handles bus schedule management. */
public class BusScheduleDAO {

    private static final String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}";

    public void addSchedule(int busId, int routeId, String depTime, String arrTime, String date) {
        if (date == null || !date.matches(DATE_REGEX)) {
            System.out.println("  Invalid date! Use format YYYY-MM-DD (e.g. 2026-03-25)."); return;
        }

        // Verify bus and route exist
        if (!recordExists("SELECT 1 FROM buses WHERE bus_id=?", busId)) {
            System.out.println("  Bus ID " + busId + " not found!"); return;
        }
        if (!recordExists("SELECT 1 FROM routes WHERE route_id=?", routeId)) {
            System.out.println("  Route ID " + routeId + " not found!"); return;
        }

        String sql = "INSERT INTO bus_schedule (bus_id, route_id, departure_time, arrival_time, travel_date) "
                   + "VALUES (?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, busId);
            ps.setInt(2, routeId);
            ps.setString(3, depTime);
            ps.setString(4, arrTime);
            ps.setString(5, date);
            ps.executeUpdate();
            System.out.println("  Schedule added successfully.");
        } catch (Exception e) {
            System.out.println("  ERROR: Could not add schedule. Check date format and values.");
        }
    }

    /** View all current and future schedules. */
    public void viewSchedules() {
        String sql = "SELECT bs.schedule_id, bs.bus_id, bs.route_id, r.source, r.destination, "
                   + "bs.departure_time, bs.arrival_time, bs.travel_date, r.base_fare "
                   + "FROM bus_schedule bs JOIN routes r ON bs.route_id = r.route_id "
                   + "WHERE bs.travel_date >= TRUNC(SYSDATE) "
                   + "ORDER BY bs.travel_date, bs.departure_time";

        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n  ======== UPCOMING BUS SCHEDULES ========");
            System.out.printf("  %-5s %-6s %-8s %-14s %-14s %-10s %-10s %-12s %-10s%n",
                    "Sch", "Bus", "Route", "From", "To", "Departs", "Arrives", "Date", "Fare");
            System.out.println("  " + "------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-5d %-6d %-8d %-14s %-14s %-10s %-10s %-12s Rs.%-7.2f%n",
                        rs.getInt("schedule_id"),
                        rs.getInt("bus_id"),
                        rs.getInt("route_id"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getString("departure_time"),
                        rs.getString("arrival_time"),
                        rs.getString("travel_date"),
                        rs.getDouble("base_fare"));
            }
            if (!found) System.out.println("  No upcoming schedules found.");

        } catch (Exception e) {
            System.out.println("  ERROR: Could not fetch schedules.");
        }
    }

    /** Returns the bus_id for a given schedule, or -1 if not found. */
    public int getBusIdForSchedule(int scheduleId) {
        String sql = "SELECT bus_id FROM bus_schedule WHERE schedule_id=?";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("bus_id");
        } catch (Exception e) {
            System.out.println("  ERROR: Could not fetch bus for schedule.");
        }
        return -1;
    }

    private boolean recordExists(String sql, int id) {
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
}