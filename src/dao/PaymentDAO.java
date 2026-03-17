package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import bus_ticket.DBconnection;

/** Handles payment persistence and reporting. */
public class PaymentDAO {

    /** Saves a payment record for a completed booking. Returns true on success. */
    public boolean savePayment(int bookingId, double amount, String mode) {
        String sql = "INSERT INTO payments (booking_id, amount, payment_mode, payment_status) "
                   + "VALUES (?, ?, ?, 'SUCCESS')";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setDouble(2, amount);
            ps.setString(3, mode);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("  WARNING: Payment record could not be saved. Booking is confirmed.");
        }
        return false;
    }

    /** Displays all payment records (admin view). */
    public void viewPayments() {
        String sql = "SELECT payment_id, booking_id, amount, payment_mode, payment_status "
                   + "FROM payments ORDER BY payment_id";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n  ========== PAYMENT RECORDS ==========");
            System.out.printf("  %-10s %-12s %-12s %-12s %-10s%n",
                    "PaymentID", "BookingID", "Amount", "Mode", "Status");
            System.out.println("  " + "----------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-10d %-12d Rs.%-9.2f %-12s %-10s%n",
                        rs.getInt("payment_id"),
                        rs.getInt("booking_id"),
                        rs.getDouble("amount"),
                        rs.getString("payment_mode"),
                        rs.getString("payment_status"));
            }
            if (!found) System.out.println("  No payment records found.");

        } catch (Exception e) {
            System.out.println("  ERROR: Could not fetch payments.");
        }
    }
}