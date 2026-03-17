package bus_ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import dao.BookingDAO;
import dao.BusScheduleDAO;
import dao.PaymentDAO;
import dao.RouteDAO;
import dao.SeatDAO;
import dao.UserDAO;
import model.User;

/**
 * User panel — self-service booking, viewing, and cancellation.
 */
public class MainAppUser {

    private static final UserDAO        userDAO     = new UserDAO();
    private static final RouteDAO       routeDAO    = new RouteDAO();
    private static final BusScheduleDAO scheduleDAO = new BusScheduleDAO();
    private static final BookingDAO     bookingDAO  = new BookingDAO();
    private static final PaymentDAO     paymentDAO  = new PaymentDAO();
    private static final SeatDAO        seatDAO     = new SeatDAO();

    public static void start(Scanner sc) {

        while (true) {
            System.out.println("\n  ======== USER PANEL ========");
            System.out.println("  1. Register");
            System.out.println("  2. Login");
            System.out.println("  3. Back");
            System.out.print("  Choose (1-3): ");

            if (!sc.hasNextInt()) {
                if (!sc.hasNextLine()) return;
                sc.nextLine(); System.out.println("  Enter 1, 2 or 3."); continue;
            }
            int choice = sc.nextInt(); sc.nextLine();

            switch (choice) {
                case 1: register(sc);  break;
                case 2: login(sc);     break;
                case 3: return;
                default: System.out.println("  Enter 1, 2 or 3.");
            }
        }
    }

    // ── REGISTER ─────────────────────────────────────────────────

    private static void register(Scanner sc) {
        System.out.println("\n  -- New User Registration --");
        User user = new User();

        System.out.print("  Full name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("  Name cannot be empty!"); return; }
        user.setName(name);

        System.out.print("  Email: ");
        String email = sc.nextLine().trim();
        if (!UserDAO.isValidEmail(email)) { System.out.println("  Invalid email! Format: name@example.com"); return; }
        user.setEmail(email);

        System.out.print("  Password (min 6 characters): ");
        String password = sc.nextLine();
        if (!UserDAO.isValidPassword(password)) { System.out.println("  Password must be at least 6 characters!"); return; }
        user.setPassword(password);

        System.out.print("  Phone (10 digits, no leading 0): ");
        String phone = sc.nextLine().trim();
        if (!UserDAO.isValidPhone(phone)) { System.out.println("  Invalid phone! 10 digits, cannot start with 0."); return; }
        user.setPhone(phone);

        if (userDAO.registerUser(user))
            System.out.println("  Registration successful! You can now log in.");
        else
            System.out.println("  Registration failed. Please try again.");
    }

    // ── LOGIN ─────────────────────────────────────────────────────

    private static void login(Scanner sc) {
        System.out.println("\n  -- User Login --");
        System.out.print("  Email: ");
        String email = sc.nextLine().trim();
        System.out.print("  Password: ");
        String password = sc.nextLine();

        int userId = userDAO.loginUser(email, password);
        if (userId == -1) {
            System.out.println("  Invalid email or password. Please try again.");
            return;
        }
        System.out.println("  Login successful! Welcome back.");
        userMenu(sc, userId);
    }

    // ── USER MENU ─────────────────────────────────────────────────

    private static void userMenu(Scanner sc, int userId) {
        while (true) {
            System.out.println("\n  ------ USER MENU ------");
            System.out.println("  1. View Routes & Fares");
            System.out.println("  2. View Schedules");
            System.out.println("  3. View Seats");
            System.out.println("  4. Book Ticket");
            System.out.println("  5. Cancel Ticket");
            System.out.println("  6. My Tickets");
            System.out.println("  7. Logout");
            System.out.print("  Choose (1-7): ");

            if (!sc.hasNextInt()) {
                if (!sc.hasNextLine()) return;
                sc.nextLine(); System.out.println("  Enter a number 1-7."); continue;
            }
            int ch = sc.nextInt(); sc.nextLine();

            switch (ch) {
                case 1: routeDAO.viewRoutes();              break;
                case 2: scheduleDAO.viewSchedules();        break;
                case 3:
                    System.out.print("  Enter Bus ID: ");
                    if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid Bus ID."); break; }
                    seatDAO.viewSeats(sc.nextInt()); sc.nextLine();
                    break;
                case 4: bookTicketFlow(sc, userId);         break;
                case 5:
                    System.out.print("  Enter Ticket ID (e.g. TKT12345): ");
                    String tid = sc.nextLine().trim();
                    if (tid.isEmpty()) { System.out.println("  Ticket ID cannot be empty!"); break; }
                    bookingDAO.cancelByTicketId(tid);
                    break;
                case 6: bookingDAO.viewMyTickets(userId);   break;
                case 7: System.out.println("  Logged out successfully."); return;
                default: System.out.println("  Enter a number 1-7.");
            }
        }
    }

    // ── BOOK TICKET FLOW ─────────────────────────────────────────────

    private static void bookTicketFlow(Scanner sc, int userId) {

        // Step 1: Choose schedule
        System.out.println("\n  -- Step 1: Available Schedules --");
        scheduleDAO.viewSchedules();
        System.out.print("  Enter Schedule ID: ");
        if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid Schedule ID."); return; }
        int scheduleId = sc.nextInt(); sc.nextLine();

        int routeId = routeDAO.getRouteIdForSchedule(scheduleId);
        if (routeId == -1) { System.out.println("  Schedule ID not found."); return; }

        // Step 2: Choose boarding & dropping stops
        List<String[]> stops = routeDAO.getStops(routeId);
        if (stops.isEmpty()) { System.out.println("  No stops found for this route."); return; }

        System.out.println("\n  -- Step 2: Choose Stops --");
        System.out.printf("  %-5s %-22s %-15s%n", "No.", "Stop Name", "Fare from Start");
        System.out.println("  " + "------------------------------------------");
        for (int i = 0; i < stops.size(); i++) {
            double f = Double.parseDouble(stops.get(i)[1]);
            System.out.printf("  %-5d %-22s %-15s%n",
                    (i + 1), stops.get(i)[0], f == 0 ? "Starting Point" : String.format("Rs.%.2f", f));
        }

        System.out.print("  Boarding stop number: ");
        if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid input."); return; }
        int boardingIdx = sc.nextInt() - 1; sc.nextLine();

        System.out.print("  Dropping stop number: ");
        if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid input."); return; }
        int droppingIdx = sc.nextInt() - 1; sc.nextLine();

        if (boardingIdx < 0 || droppingIdx >= stops.size() || boardingIdx >= droppingIdx) {
            System.out.println("  Invalid stop selection. Dropping must be after boarding."); return;
        }

        String boardingStop = stops.get(boardingIdx)[0];
        String droppingStop = stops.get(droppingIdx)[0];
        double farePerPerson = routeDAO.calculateFare(routeId, boardingStop, droppingStop);

        System.out.println("\n  Boarding   : " + boardingStop);
        System.out.println("  Dropping   : " + droppingStop);
        System.out.printf ("  Fare/Person: Rs.%.2f%n", farePerPerson);

        // Step 3: Passenger count
        System.out.print("\n  -- Step 3: Number of passengers (1-5): ");
        if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid input."); return; }
        int memberCount = sc.nextInt(); sc.nextLine();
        if (memberCount < 1 || memberCount > 5) { System.out.println("  Must be between 1 and 5."); return; }

        // Step 4: Seat selection
        System.out.println("\n  -- Step 4: Select Seats --");
        int busId = scheduleDAO.getBusIdForSchedule(scheduleId);
        seatDAO.viewSeats(busId);

        List<int[]> selectedSeats = new ArrayList<>();
        for (int i = 0; i < memberCount; i++) {
            while (true) {
                System.out.print("  Seat for Passenger " + (i + 1) + ": ");
                if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid. Enter a seat number."); continue; }
                int seat = sc.nextInt(); sc.nextLine();

                // Duplicate check
                boolean dup = false;
                for (int[] s : selectedSeats) { if (s[0] == seat) { dup = true; break; } }
                if (dup) { System.out.println("  Seat " + seat + " already selected. Choose another."); continue; }

                // Status check (AVAILABLE / BOOKED / INVALID)
                String status = bookingDAO.getSeatStatus(scheduleId, seat);
                if ("INVALID".equals(status)) {
                    System.out.println("  Seat " + seat + " does not exist on this bus. Enter a valid seat number.");
                } else if ("BOOKED".equals(status)) {
                    System.out.println("  Seat " + seat + " is already booked. Choose another.");
                } else {
                    selectedSeats.add(new int[]{seat});
                    System.out.println("  Seat " + seat + " selected.");
                    break;
                }
            }
        }

        // Step 5: Passenger details with validation
        System.out.println("\n  -- Step 5: Passenger Details --");
        List<String[]> passengers = new ArrayList<>();
        for (int i = 0; i < memberCount; i++) {
            System.out.println("  Passenger " + (i + 1) + " (Seat " + selectedSeats.get(i)[0] + "):");
            String pName, pPhone, pAge, pGender;

            // Name — non-empty
            while (true) {
                System.out.print("    Name   : ");
                pName = sc.nextLine().trim();
                if (!pName.isEmpty()) break;
                System.out.println("    Name cannot be empty!");
            }

            // Phone — 10 digits
            while (true) {
                System.out.print("    Phone  : ");
                pPhone = sc.nextLine().trim();
                if (pPhone.matches("[0-9]{10}")) break;
                System.out.println("    Invalid phone! Enter exactly 10 digits.");
            }

            // Age — 1 to 120
            while (true) {
                System.out.print("    Age    : ");
                pAge = sc.nextLine().trim();
                try {
                    int age = Integer.parseInt(pAge);
                    if (age >= 1 && age <= 120) break;
                } catch (NumberFormatException ignored) {}
                System.out.println("    Invalid age! Enter a number between 1 and 120.");
            }

            // Gender
            while (true) {
                System.out.print("    Gender (Male/Female/Other): ");
                pGender = sc.nextLine().trim();
                if (pGender.equalsIgnoreCase("Male") || pGender.equalsIgnoreCase("Female") || pGender.equalsIgnoreCase("Other")) break;
                System.out.println("    Enter Male, Female, or Other.");
            }

            passengers.add(new String[]{pName, pPhone, pAge, pGender});
        }

        // Step 6: Payment
        double totalFare = farePerPerson * memberCount;
        System.out.println("\n  -- Step 6: Payment --");
        System.out.printf("  Passengers  : %d%n", memberCount);
        System.out.printf("  Fare/Person : Rs.%.2f%n", farePerPerson);
        System.out.printf("  Total Fare  : Rs.%.2f%n", totalFare);
        System.out.println("  Payment     : 1. Cash   2. UPI   3. Card");
        System.out.print("  Choose mode : ");
        if (!sc.hasNextInt()) { sc.nextLine(); System.out.println("  Invalid payment mode."); return; }
        int modeChoice = sc.nextInt(); sc.nextLine();

        String mode;
        switch (modeChoice) {
            case 1: mode = "Cash"; break;
            case 2: mode = "UPI";  break;
            case 3: mode = "Card"; break;
            default: System.out.println("  Invalid payment mode!"); return;
        }

        System.out.printf("  Confirm Rs.%.2f via %s? (yes/y to confirm): ", totalFare, mode);
        String confirm = sc.nextLine().trim();
        if (!confirm.equalsIgnoreCase("yes") && !confirm.equalsIgnoreCase("y")) {
            System.out.println("  Payment cancelled. Booking not confirmed.");
            return;
        }

        System.out.println("  Processing payment...");

        int bookingId = bookingDAO.bookTicket(userId, scheduleId, boardingStop, droppingStop,
                totalFare, selectedSeats, passengers);

        if (bookingId != -1) {
            paymentDAO.savePayment(bookingId, totalFare, mode);
            System.out.println("  Booking confirmed! Your ticket details are shown above.");
        } else {
            System.out.println("  Booking failed. Please try again or contact admin.");
        }
    }
}