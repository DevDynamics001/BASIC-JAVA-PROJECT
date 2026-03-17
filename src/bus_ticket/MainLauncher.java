package bus_ticket;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Entry point for the Bus Ticket Booking System.
 * Verifies DB connectivity before starting the application.
 */
public class MainLauncher {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║    BUS TICKET BOOKING SYSTEM v1.0   ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("  Connecting to database...");

        if (!DBconnection.testConnection()) {
            System.out.println(" FAILED");
            System.out.println("\n  ERROR: Cannot connect to Oracle database.");
            System.out.println("  Please ensure Oracle XE is running on localhost:1521.");
            System.exit(1);
        }
        System.out.println(" OK");

        Scanner sc = new Scanner(System.in);

        try {
            while (true) {
                System.out.println("\n==============================");
                System.out.println("   BUS TICKET BOOKING SYSTEM");
                System.out.println("==============================");
                System.out.println("1. Admin Login");
                System.out.println("2. User Login / Register");
                System.out.println("3. Exit");
                System.out.print("Choose option: ");

                if (!sc.hasNextInt()) {
                    if (!sc.hasNextLine()) break; // EOF — exit cleanly
                    sc.nextLine();
                    System.out.println("Invalid input! Enter 1, 2 or 3.");
                    continue;
                }
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1: MainAppAdmin.start(sc); break;
                    case 2: MainAppUser.start(sc);  break;
                    case 3:
                        System.out.println("Thank you for using Bus Ticket Booking System. Goodbye!");
                        sc.close();
                        return;
                    default:
                        System.out.println("Invalid option! Enter 1, 2 or 3.");
                }
            }
        } catch (NoSuchElementException e) {
            // stdin closed or piped input exhausted — exit cleanly
        }
        System.out.println("Goodbye!");
        sc.close();
    }
}