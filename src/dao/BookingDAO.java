package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import bus_ticket.DBconnection;

/**
 * Handles all booking lifecycle operations:
 * create booking, view bookings, cancel booking, view my tickets.
 */
public class BookingDAO {

    // ── SEAT AVAILABILITY ─────────────────────────────────────────

    /**
     * Returns "AVAILABLE", "BOOKED", or "INVALID" for a seat on a schedule.
     * INVALID means the seat number does not exist at all (out of range).
     */
    public String getSeatStatus(int scheduleId, int seatNumber) {
        String sql = "SELECT seat_status FROM seats "
                   + "WHERE bus_id=(SELECT bus_id FROM bus_schedule WHERE schedule_id=?) "
                   + "AND seat_number=?";
        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            ps.setInt(2, seatNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("seat_status");
        } catch (Exception e) {
            System.out.println("  ERROR: Could not check seat status.");
        }
        return "INVALID"; // seat doesn't exist in the table
    }

    /** Convenience check — returns true only if seat is AVAILABLE. */
    public boolean isSeatAvailable(int scheduleId, int seatNumber) {
        return "AVAILABLE".equals(getSeatStatus(scheduleId, seatNumber));
    }

    // ── BOOK TICKET ───────────────────────────────────────────────

    /**
     * Creates a booking and records all passenger details.
     * passengers: each String[] = { name, phone, age, gender }
     * Returns booking ID on success, -1 on failure.
     */
    public int bookTicket(int userId, int scheduleId,
                           String boardingStop, String droppingStop,
                           double totalFare, List<int[]> seatNumbers,
                           List<String[]> passengers) {

        String insertBooking   = "INSERT INTO bookings (user_id, schedule_id, seat_number, booking_date, "
                               + "boarding_stop, dropping_stop, total_fare, payment_done) "
                               + "VALUES (?, ?, ?, SYSDATE, ?, ?, ?, 1)";
        String getBookingId    = "SELECT bookings_seq.CURRVAL FROM dual";
        String insertPassenger = "INSERT INTO ticket_passengers "
                               + "(ticket_id, booking_id, passenger_name, age, gender, phone, seat_number, fare) "
                               + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSeat      = "UPDATE seats SET seat_status='BOOKED' WHERE seat_number=? "
                               + "AND bus_id=(SELECT bus_id FROM bus_schedule WHERE schedule_id=?)";

        String ticketId = generateTicketId();
        double farePerPerson = totalFare / passengers.size();

        try (Connection con = DBconnection.getConnection()) {

            // Insert booking record
            PreparedStatement ps1 = con.prepareStatement(insertBooking);
            ps1.setInt(1, userId);
            ps1.setInt(2, scheduleId);
            ps1.setInt(3, seatNumbers.get(0)[0]);
            ps1.setString(4, boardingStop);
            ps1.setString(5, droppingStop);
            ps1.setDouble(6, totalFare);
            ps1.executeUpdate();

            // Get the generated booking ID via Oracle sequence CURRVAL
            ResultSet rsId = con.prepareStatement(getBookingId).executeQuery();
            if (!rsId.next()) { System.out.println("  Booking failed — could not get booking ID."); return -1; }
            int bookingId = rsId.getInt(1);

            // Insert each passenger and mark seat as booked
            for (int i = 0; i < passengers.size(); i++) {
                String[] p    = passengers.get(i);
                int        seatNo = seatNumbers.get(i)[0];

                PreparedStatement psP = con.prepareStatement(insertPassenger);
                psP.setString(1, ticketId);
                psP.setInt(2, bookingId);
                psP.setString(3, p[0]);                        // name
                psP.setInt(4, Integer.parseInt(p[2]));         // age
                psP.setString(5, p[3]);                        // gender
                psP.setString(6, p[1]);                        // phone
                psP.setInt(7, seatNo);
                psP.setDouble(8, farePerPerson);
                psP.executeUpdate();

                PreparedStatement psS = con.prepareStatement(updateSeat);
                psS.setInt(1, seatNo);
                psS.setInt(2, scheduleId);
                psS.executeUpdate();
            }

            printTicket(ticketId, bookingId, scheduleId, boardingStop, droppingStop,
                        totalFare, passengers, seatNumbers);
            return bookingId;

        } catch (Exception e) {
            System.out.println("  ERROR: Booking failed. " + e.getMessage());
        }
        return -1;
    }

    // ── PRINT TICKET ──────────────────────────────────────────────

    private void printTicket(String ticketId, int bookingId, int scheduleId,
                              String boarding, String dropping, double totalFare,
                              List<String[]> passengers, List<int[]> seats) {

        double farePerPerson = totalFare / passengers.size();
        System.out.println("\n  ╔══════════════════════════════════════════╗");
        System.out.println("  ║         BUS TICKET — CONFIRMED           ║");
        System.out.println("  ╚══════════════════════════════════════════╝");
        System.out.println("  Ticket ID    : " + ticketId);
        System.out.println("  Booking ID   : " + bookingId);
        System.out.println("  Schedule ID  : " + scheduleId);
        System.out.println("  Boarding     : " + boarding);
        System.out.println("  Dropping     : " + dropping);
        System.out.printf ("  Total Fare   : Rs.%.2f%n", totalFare);
        System.out.println("  Passengers   : " + passengers.size());
        System.out.println("  " + "------------------------------------------------");
        System.out.printf ("  %-5s %-20s %-5s %-8s %-10s%n", "Seat", "Name", "Age", "Gender", "Fare");
        System.out.println("  " + "------------------------------------------------");
        for (int i = 0; i < passengers.size(); i++) {
            String[] p = passengers.get(i);
            System.out.printf("  %-5d %-20s %-5s %-8s Rs.%.2f%n",
                    seats.get(i)[0], p[0], p[2], p[3], farePerPerson);
        }
        System.out.println("  " + "================================================");
        System.out.println("  SAVE YOUR TICKET ID: " + ticketId);
        System.out.println("  " + "================================================");
    }

    // ── VIEW ALL BOOKINGS (ADMIN) ─────────────────────────────────

    public void viewBookings() {
        String sql = "SELECT b.booking_id, b.user_id, b.schedule_id, b.seat_number, "
                   + "b.booking_date, b.boarding_stop, b.dropping_stop, b.total_fare, "
                   + "(SELECT MIN(t.ticket_id) FROM ticket_passengers t WHERE t.booking_id = b.booking_id) AS ticket_id "
                   + "FROM bookings b ORDER BY b.booking_id";

        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n  ========== ALL BOOKINGS ==========");
            System.out.printf("  %-10s %-12s %-8s %-12s %-14s %-14s %-12s%n",
                    "BookingID", "TicketID", "UserID", "ScheduleID", "Boarding", "Dropping", "TotalFare");
            System.out.println("  " + "------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                String tid = rs.getString("ticket_id");
                System.out.printf("  %-10d %-12s %-8d %-12d %-14s %-14s Rs.%.2f%n",
                        rs.getInt("booking_id"),
                        tid != null ? tid : "N/A",
                        rs.getInt("user_id"),
                        rs.getInt("schedule_id"),
                        rs.getString("boarding_stop"),
                        rs.getString("dropping_stop"),
                        rs.getDouble("total_fare"));
            }
            if (!found) System.out.println("  No bookings found.");

        } catch (Exception e) {
            System.out.println("  ERROR: Could not fetch bookings.");
        }
    }

    // ── CANCEL BY BOOKING ID (ADMIN) ─────────────────────────────

    public void cancelBooking(int bookingId) {
        String checkBooking     = "SELECT seat_number, schedule_id FROM bookings WHERE booking_id=?";
        String getTicket        = "SELECT DISTINCT ticket_id FROM ticket_passengers WHERE booking_id=?";
        String deletePayments   = "DELETE FROM payments WHERE booking_id=?";
        String deletePassengers = "DELETE FROM ticket_passengers WHERE booking_id=?";
        String deleteBooking    = "DELETE FROM bookings WHERE booking_id=?";
        String updateSeat       = "UPDATE seats SET seat_status='AVAILABLE' WHERE seat_number=? "
                                + "AND bus_id=(SELECT bus_id FROM bus_schedule WHERE schedule_id=?)";

        try (Connection con = DBconnection.getConnection()) {

            // Verify booking exists
            PreparedStatement psCheck = con.prepareStatement(checkBooking);
            psCheck.setInt(1, bookingId);
            ResultSet rsCheck = psCheck.executeQuery();
            if (!rsCheck.next()) { System.out.println("  Booking ID " + bookingId + " not found!"); return; }
            int seatNumber = rsCheck.getInt("seat_number");
            int scheduleId = rsCheck.getInt("schedule_id");

            // Try cancelling via ticket ID (normal flow with passengers)
            PreparedStatement psTicket = con.prepareStatement(getTicket);
            psTicket.setInt(1, bookingId);
            ResultSet rsTicket = psTicket.executeQuery();
            if (rsTicket.next()) {
                cancelByTicketId(rsTicket.getString("ticket_id")); return;
            }

            // No passengers — cancel directly
            PreparedStatement psSeat = con.prepareStatement(updateSeat);
            psSeat.setInt(1, seatNumber);
            psSeat.setInt(2, scheduleId);
            psSeat.executeUpdate();

            PreparedStatement psPay = con.prepareStatement(deletePayments);
            psPay.setInt(1, bookingId);
            psPay.executeUpdate();

            PreparedStatement psPass = con.prepareStatement(deletePassengers);
            psPass.setInt(1, bookingId);
            psPass.executeUpdate();

            PreparedStatement psDel = con.prepareStatement(deleteBooking);
            psDel.setInt(1, bookingId);
            psDel.executeUpdate();

            System.out.println("  Booking ID " + bookingId + " cancelled. Seat " + seatNumber + " released.");

        } catch (Exception e) {
            System.out.println("  ERROR: Could not cancel booking.");
        }
    }

    // ── CANCEL BY TICKET ID (USER & ADMIN) ───────────────────────

    public void cancelByTicketId(String ticketId) {
        String getBookingId     = "SELECT DISTINCT booking_id FROM ticket_passengers WHERE ticket_id=?";
        String getSeats         = "SELECT seat_number FROM ticket_passengers WHERE ticket_id=?";
        String getSchedule      = "SELECT schedule_id FROM bookings WHERE booking_id=?";
        String deletePayments   = "DELETE FROM payments WHERE booking_id=?";
        String deletePassengers = "DELETE FROM ticket_passengers WHERE ticket_id=?";
        String deleteBooking    = "DELETE FROM bookings WHERE booking_id=?";
        String updateSeat       = "UPDATE seats SET seat_status='AVAILABLE' WHERE seat_number=? "
                                + "AND bus_id=(SELECT bus_id FROM bus_schedule WHERE schedule_id=?)";

        try (Connection con = DBconnection.getConnection()) {

            PreparedStatement ps1 = con.prepareStatement(getBookingId);
            ps1.setString(1, ticketId);
            ResultSet rs1 = ps1.executeQuery();
            if (!rs1.next()) { System.out.println("  Ticket ID '" + ticketId + "' not found!"); return; }
            int bookingId = rs1.getInt("booking_id");

            PreparedStatement psSchedule = con.prepareStatement(getSchedule);
            psSchedule.setInt(1, bookingId);
            ResultSet rsSchedule = psSchedule.executeQuery();
            int scheduleId = rsSchedule.next() ? rsSchedule.getInt("schedule_id") : -1;

            // Release all seats on this ticket
            PreparedStatement psSeats = con.prepareStatement(getSeats);
            psSeats.setString(1, ticketId);
            ResultSet rsSeats = psSeats.executeQuery();
            while (rsSeats.next()) {
                PreparedStatement psUpdate = con.prepareStatement(updateSeat);
                psUpdate.setInt(1, rsSeats.getInt("seat_number"));
                psUpdate.setInt(2, scheduleId);
                psUpdate.executeUpdate();
            }

            PreparedStatement psPay = con.prepareStatement(deletePayments);
            psPay.setInt(1, bookingId);
            psPay.executeUpdate();

            PreparedStatement psDel1 = con.prepareStatement(deletePassengers);
            psDel1.setString(1, ticketId);
            psDel1.executeUpdate();

            PreparedStatement psDel2 = con.prepareStatement(deleteBooking);
            psDel2.setInt(1, bookingId);
            psDel2.executeUpdate();

            System.out.println("  Ticket '" + ticketId + "' cancelled. All seats released.");

        } catch (Exception e) {
            System.out.println("  ERROR: Could not cancel ticket.");
        }
    }

    // ── VIEW MY TICKETS (USER) ────────────────────────────────────

    public void viewMyTickets(int userId) {
        String sql = "SELECT b.booking_id, b.schedule_id, b.boarding_stop, b.dropping_stop, "
                   + "b.total_fare, b.booking_date, tp.ticket_id, tp.passenger_name, "
                   + "tp.age, tp.gender, tp.seat_number, tp.fare "
                   + "FROM bookings b "
                   + "JOIN ticket_passengers tp ON b.booking_id = tp.booking_id "
                   + "WHERE b.user_id=? ORDER BY b.booking_id, tp.seat_number";

        try (Connection con = DBconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n  ╔══════════════════════════════════════════╗");
            System.out.println("  ║            MY BOOKED TICKETS             ║");
            System.out.println("  ╚══════════════════════════════════════════╝");

            boolean found = false;
            int lastBookingId = -1;

            while (rs.next()) {
                found = true;
                int bookingId = rs.getInt("booking_id");
                if (bookingId != lastBookingId) {
                    lastBookingId = bookingId;
                    System.out.println("\n  Ticket ID    : " + rs.getString("ticket_id"));
                    System.out.println("  Booking ID   : " + bookingId);
                    System.out.println("  Schedule ID  : " + rs.getInt("schedule_id"));
                    System.out.println("  Boarding     : " + rs.getString("boarding_stop"));
                    System.out.println("  Dropping     : " + rs.getString("dropping_stop"));
                    System.out.printf ("  Total Fare   : Rs.%.2f%n", rs.getDouble("total_fare"));
                    System.out.println("  Booked On    : " + rs.getTimestamp("booking_date"));
                    System.out.println("  -- Passengers --");
                    System.out.printf ("  %-5s %-20s %-5s %-10s %-10s%n",
                            "Seat", "Name", "Age", "Gender", "Fare");
                    System.out.println("  " + "--------------------------------------------------");
                }
                System.out.printf("  %-5d %-20s %-5d %-10s Rs.%.2f%n",
                        rs.getInt("seat_number"),
                        rs.getString("passenger_name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getDouble("fare"));
            }

            if (!found) {
                System.out.println("\n  No tickets found. Book a ticket to get started!");
            } else {
                System.out.println("\n  " + "==================================================");
                System.out.println("  Use 'Cancel Ticket' with your Ticket ID to cancel.");
            }

        } catch (Exception e) {
            System.out.println("  ERROR: Could not fetch your tickets.");
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────

    private String generateTicketId() {
        return "TKT" + (System.currentTimeMillis() % 100000);
    }
}