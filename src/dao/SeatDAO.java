package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bus_ticket.DBconnection;

/** Handles seat availability display. */
public class SeatDAO {

    public void viewSeats(int busId) {
        String countSql = "SELECT COUNT(*) AS total FROM seats WHERE bus_id=?";
        String seatSql  = "SELECT seat_number, seat_status FROM seats WHERE bus_id=? ORDER BY seat_number";

        try (Connection con = DBconnection.getConnection()) {

            PreparedStatement psCount = con.prepareStatement(countSql);
            psCount.setInt(1, busId);
            ResultSet rsCount = psCount.executeQuery();
            int totalSeats = rsCount.next() ? rsCount.getInt("total") : 0;

            if (totalSeats == 0) {
                System.out.println("  No seats found for Bus ID " + busId + "."); return;
            }

            PreparedStatement ps = con.prepareStatement(seatSql);
            ps.setInt(1, busId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n  ===== SEAT AVAILABILITY — BUS " + busId + " =====");
            System.out.println("  Total Seats: " + totalSeats + "   [A]=Available  [X]=Booked");
            System.out.println("  " + "----------------------------------------------------");

            int count = 0, available = 0, booked = 0;
            while (rs.next()) {
                String status = rs.getString("seat_status");
                boolean isAvail = "AVAILABLE".equals(status);
                System.out.printf("  Seat %-3d [%s]   ", rs.getInt("seat_number"), isAvail ? "A" : "X");
                count++;
                if (count % 5 == 0) System.out.println();
                if (isAvail) available++; else booked++;
            }
            if (count % 5 != 0) System.out.println();

            System.out.println("  " + "----------------------------------------------------");
            System.out.println("  Available: " + available + "   Booked: " + booked);
            System.out.println("  " + "====================================================");

        } catch (Exception e) {
            System.out.println("  ERROR: Could not fetch seat data for Bus " + busId + ".");
        }
    }
}