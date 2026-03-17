package bus_ticket;

import java.util.NoSuchElementException;
import java.util.Scanner;

import dao.AdminDAO;
import dao.BookingDAO;
import dao.BusDAO;
import dao.BusScheduleDAO;
import dao.PaymentDAO;
import dao.RouteDAO;
import dao.SeatDAO;

/**
 * Admin panel — provides full system management capabilities.
 * Manages routes, buses, schedules, bookings, payments, and seats.
 */
public class MainAppAdmin {

    private static final AdminDAO       adminDAO    = new AdminDAO();
    private static final RouteDAO       routeDAO    = new RouteDAO();
    private static final BusDAO         busDAO      = new BusDAO();
    private static final BusScheduleDAO scheduleDAO = new BusScheduleDAO();
    private static final BookingDAO     bookingDAO  = new BookingDAO();
    private static final PaymentDAO     paymentDAO  = new PaymentDAO();
    private static final SeatDAO        seatDAO     = new SeatDAO();

    public static void start(Scanner sc) {

        System.out.println("\n  ======= ADMIN LOGIN =======");
        System.out.print("  Username: ");
        String username = sc.nextLine().trim();
        System.out.print("  Password: ");
        String password = sc.nextLine();

        if (!adminDAO.loginAdmin(username, password)) {
            System.out.println("  Invalid credentials. Access denied.");
            return;
        }
        System.out.println("  Admin login successful. Welcome!");

        try { while (true) {
            System.out.println("\n  ======== ADMIN PANEL ========");
            System.out.println("  1. Manage Routes");
            System.out.println("  2. Manage Buses");
            System.out.println("  3. Manage Schedules");
            System.out.println("  4. Manage Bookings");
            System.out.println("  5. View Payments");
            System.out.println("  6. View Seats");
            System.out.println("  7. Logout");
            System.out.print("  Choose (1-7): ");

            if (!sc.hasNextInt()) {
                if (!sc.hasNextLine()) break;
                sc.nextLine(); System.out.println("  Enter a number 1-7."); continue;
            }
            int choice = sc.nextInt(); sc.nextLine();

            switch (choice) {
                case 1: manageRoutes(sc);    break;
                case 2: manageBuses(sc);     break;
                case 3: manageSchedules(sc); break;
                case 4: manageBookings(sc);  break;
                case 5: paymentDAO.viewPayments(); break;
                case 6:
                    System.out.print("  Enter Bus ID: ");
                    if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid Bus ID."); break; }
                    seatDAO.viewSeats(sc.nextInt()); sc.nextLine();
                    break;
                case 7: System.out.println("  Logged out."); return;
                default: System.out.println("  Invalid option. Choose 1-7.");
            }
        } } catch (NoSuchElementException e) { /* stdin closed */ }
    }

    // ── MANAGE ROUTES ─────────────────────────────────────────────

    private static void manageRoutes(Scanner sc) {
        System.out.println("\n  -- Route Management --");
        System.out.println("  1. Add Route");
        System.out.println("  2. View Routes");
        System.out.println("  3. Delete Route");
        System.out.print("  Choose: ");
        if (!sc.hasNextInt()) { sc.nextLine(); return; }
        int ch = sc.nextInt(); sc.nextLine();

        if (ch == 1) {
            System.out.print("  Source city: ");
            String src = sc.nextLine().trim();
            System.out.print("  Destination city: ");
            String dst = sc.nextLine().trim();
            System.out.print("  Distance (KM): ");
            if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid distance."); return; }
            int dist = sc.nextInt(); sc.nextLine();
            System.out.print("  Base fare (Rs): ");
            if (!sc.hasNextDouble()) { sc.nextLine(); System.out.println("  Invalid fare."); return; }
            double fare = sc.nextDouble(); sc.nextLine();
            routeDAO.addRoute(src, dst, dist, fare);

        } else if (ch == 2) {
            routeDAO.viewRoutes();

        } else if (ch == 3) {
            routeDAO.viewRoutes();
            System.out.print("  Enter Route ID to delete: ");
            if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid ID."); return; }
            routeDAO.deleteRoute(sc.nextInt()); sc.nextLine();
        }
    }

    // ── MANAGE BUSES ──────────────────────────────────────────────

    private static void manageBuses(Scanner sc) {
        System.out.println("\n  -- Bus Management --");
        System.out.println("  1. Add Bus");
        System.out.println("  2. View Buses");
        System.out.println("  3. Delete Bus");
        System.out.print("  Choose: ");
        if (!sc.hasNextInt()) { sc.nextLine(); return; }
        int ch = sc.nextInt(); sc.nextLine();

        if (ch == 1) {
            System.out.print("  Bus name: ");
            String name = sc.nextLine().trim();
            System.out.print("  Bus type (e.g. AC Sleeper, Non-AC Seater): ");
            String type = sc.nextLine().trim();
            System.out.print("  Total seats: ");
            if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid seat count."); return; }
            int seats = sc.nextInt(); sc.nextLine();
            busDAO.addBus(name, type, seats);

        } else if (ch == 2) {
            busDAO.viewBuses();

        } else if (ch == 3) {
            busDAO.viewBuses();
            System.out.print("  Enter Bus ID to delete: ");
            if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid ID."); return; }
            busDAO.deleteBus(sc.nextInt()); sc.nextLine();
        }
    }

    // ── MANAGE SCHEDULES ──────────────────────────────────────────

    private static void manageSchedules(Scanner sc) {
        System.out.println("\n  -- Schedule Management --");
        System.out.println("  1. Add Schedule");
        System.out.println("  2. View Schedules");
        System.out.print("  Choose: ");
        if (!sc.hasNextInt()) { sc.nextLine(); return; }
        int ch = sc.nextInt(); sc.nextLine();

        if (ch == 1) {
            System.out.print("  Bus ID: ");
            if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid Bus ID."); return; }
            int busId = sc.nextInt();
            System.out.print("  Route ID: ");
            if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid Route ID."); return; }
            int routeId = sc.nextInt(); sc.nextLine();
            System.out.print("  Departure time (HH:MM): ");
            String dep = sc.nextLine().trim();
            System.out.print("  Arrival time (HH:MM): ");
            String arr = sc.nextLine().trim();
            System.out.print("  Travel date (YYYY-MM-DD): ");
            String date = sc.nextLine().trim();
            scheduleDAO.addSchedule(busId, routeId, dep, arr, date);

        } else {
            scheduleDAO.viewSchedules();
        }
    }

    // ── MANAGE BOOKINGS ───────────────────────────────────────────

    private static void manageBookings(Scanner sc) {
        System.out.println("\n  -- Booking Management --");
        System.out.println("  1. View All Bookings");
        System.out.println("  2. Cancel Booking");
        System.out.print("  Choose: ");
        if (!sc.hasNextInt()) { sc.nextLine(); return; }
        int ch = sc.nextInt(); sc.nextLine();

        if (ch == 1) {
            bookingDAO.viewBookings();
        } else if (ch == 2) {
            bookingDAO.viewBookings();
            System.out.print("  Enter Booking ID to cancel: ");
            if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid Booking ID."); return; }
            bookingDAO.cancelBooking(sc.nextInt()); sc.nextLine();
        }
    }
}